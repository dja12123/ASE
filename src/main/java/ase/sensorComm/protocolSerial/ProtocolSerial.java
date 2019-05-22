package ase.sensorComm.protocolSerial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ReceiveEvent;
import ase.util.observer.KeyObservable;

public class ProtocolSerial extends KeyObservable<Short, ReceiveEvent> implements ISensorCommManager
{
	public static final Logger logger = LogWriter.createLogger(ProtocolSerial.class, "protoSerialReader");
	public static final String PROP_SerialDevice = "SerialDevice";
	
	private final Serial serial;
	private final SerialConfig config;
	private final SerialWriter serialWriter;

	private boolean isRun;
	
	private Thread commManageThread;
	private final Runnable commManageTask;
	
	private final Map<Integer, CommUser> _users;
	private final List<CommUser> _userList;
	private final Map<Integer, CommUser> users;
	
	private TransactionUnit nowTransaction;
	private int nowUserIndex;

	public ProtocolSerial()
	{
		this.serial = SerialFactory.createInstance();
		this.serial.addListener(this::dataReceived);
		this.config = new SerialConfig();
		this.config.baud(Baud._19200).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);
		this.serialWriter = new SerialWriter(this.serial);
		this.commManageTask = this::commManageTask;
		this._users = new HashMap<>();
		this._userList = new ArrayList<>();
		this.users = Collections.unmodifiableMap(this._users);
	}
	
	public synchronized boolean startModule()
	{
		logger.log(Level.INFO, "ProtocolSerial 시작");
		this.config.device(ServerCore.getProp(PROP_SerialDevice));
		try
		{
			this.serial.open(this.config);
			logger.log(Level.INFO, "연결: " + this.config.toString());
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "시리얼 열기 실패", e);
			return false;
		}
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e1)
		{
		}
		this.serialWriter.startModule();
		this.isRun = true;
		
		this.commManageThread = new Thread(this.commManageTask);
		this.commManageThread.setDaemon(true);
		this.commManageThread.start();
		
		logger.log(Level.INFO, "ProtocolSerial 시작 완료");
		return true;
	}

	public synchronized void stopModule()
	{
		if(this.isRun)
		{
			this.isRun = false;
			this.commManageThread.interrupt();
		}
		this.serialWriter.stopModule();
		this._users.clear();
		this._userList.clear();
		this.clearObservers();
		this.nowTransaction = null;
		this.nowUserIndex = 0;
		SerialFactory.shutdown();
		logger.log(Level.INFO, "ProtocolSerial 종료");
	}
	
	@Override
	public synchronized boolean addUser(int intid)
	{
		if(intid > Byte.MAX_VALUE)
		{
			return false;
		}
		byte id = (byte)intid;
		if(this._users.containsKey(intid)) return false;
		CommUser user = new CommUser(id);
		this._users.put(intid, user);
		this._userList.add(user);
		return true;
	}
	
	@Override
	public synchronized void removeUser(int intid)
	{
		byte id = (byte)intid;
		CommUser user = this._users.getOrDefault(id, null);
		if(user != null)
		{
			if(this.nowTransaction != null && this.nowTransaction.user == user)
			{
				this.nowTransaction = null;
			}
			this._users.remove(intid);
			this._userList.remove(user);
		}
	}
	
	@Override
	public Map<Integer, CommUser> getUserMap()
	{
		return this.users;
	}
	
	private void commManageTask()
	{
		while(this.isRun)
		{
			try
			{
				Thread.sleep(SerialProtoDef.SERIAL_TRANSACTION_TIMEOUT);
			}
			catch (InterruptedException e1)
			{
				synchronized (this)
				{
					if(this.nowTransaction == null)
					{
						this.startTransaction();
					}
					continue;
				}
			}
			synchronized (this)
			{
				if(this.nowTransaction != null)
				{
					logger.log(Level.WARNING, "트랜잭션 타임아웃 " + this.nowTransaction.user.ID);
					this.nowTransaction = null;
				}
				this.startTransaction();
			}
		}
	}
	
	private void startTransaction()
	{
		if(!this._userList.isEmpty())
		{
			if(this.nowUserIndex >= this._userList.size())
			{
				this.nowUserIndex = 0;
			}
			CommUser nowUser = this._userList.get(this.nowUserIndex);
			this.nowTransaction = new TransactionUnit(System.currentTimeMillis(), nowUser);
			List<byte[]> packetList = nowUser.popData();
			if(packetList.size() == 0)
			{
				byte[] packet = new byte[2];
				packet[0] = nowUser.ID;
				packet[1] = SerialProtoDef.SERIAL_PACKET_SEG_NODATASERVER;
				this.serialWriter.write(packet);
			}
			else
			{
				for(byte[] packet : packetList)
				{
					this.serialWriter.write(packet);
				}
			}

			++this.nowUserIndex;
		}
	}
	
	
	private synchronized void dataReceived(SerialDataEvent event)
	{
		byte[] receiveData;
		try
		{
			receiveData = event.getBytes();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		if(receiveData.length > SerialProtoDef.SERIAL_PACKET_MAXSIZE)
		{
			logger.log(Level.WARNING, "수신 크기 오류 " + receiveData.length);
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(receiveData);

		byte command = buffer.get();
		byte id = buffer.get();
		
		if(this.nowTransaction == null || this.nowTransaction.user.ID != id)
		{
			return;
		}
		
		byte[] payload = new byte[receiveData.length - 1 - 1];
		buffer.get(payload, 1 + 1, payload.length);
		if(command == SerialProtoDef.SERIAL_PACKET_SEG_NODATACLIENT)
		{
			this.nowTransaction = null;
		}
		else
		{
			if(!this.nowTransaction.putReceiveData(command, payload))
			{
				return;
			}
			
			if(this.nowTransaction.isReceiveFinish())
			{
				ReceiveEvent e = new ReceiveEvent(this.nowTransaction.user.ID, this.nowTransaction.getkey(), this.nowTransaction.getPayload());
				this.notifyObservers(ServerCore.mainThreadPool, e.key, e);
				this.nowTransaction = null;
			}
		}
		this.commManageThread.interrupt();
	}

}
