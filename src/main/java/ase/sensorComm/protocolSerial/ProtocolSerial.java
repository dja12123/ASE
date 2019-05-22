package ase.sensorComm.protocolSerial;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import ase.sensorComm.ReceiveEvent;
import ase.util.observer.KeyObservable;

public class ProtocolSerial extends KeyObservable<Short, ReceiveEvent>
{
	public static final Logger logger = LogWriter.createLogger(ProtocolSerial.class, "protoSerialReader");
	public static final String PROP_SerialDevice = "SerialDevice";
	private static final int SERIAL_DELAY = 70;
	private static final int SERIAL_TRANSACTION_TIMEOUT = 500;
	
	private Serial serial;
	private SerialConfig config;

	private boolean isRun;
	private Thread commManageThread;
	private Runnable commManageTask;
	
	private final Map<Byte, CommUser> _users;
	public final Map<Byte, CommUser> users;
	
	private TransactionUnit nowTransaction;
	private byte nowUserIndex;

	public ProtocolSerial()
	{
		this.serial = SerialFactory.createInstance();
		this.serial.addListener(this::dataReceived);
		this.config = new SerialConfig();
		this.config.baud(Baud._19200).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);
		this.commManageTask = this::commManageTask;
		this._users = new HashMap<>();
		this.users = Collections.unmodifiableMap(this._users);
	}
	
	public synchronized boolean startModule()
	{
		logger.log(Level.INFO, "SerialReadManager 시작");
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
		this.isRun = true;
		this.commManageThread = new Thread(this.commManageTask);
		this.commManageThread.setDaemon(true);
		this.commManageThread.start();
		logger.log(Level.INFO, "SerialReadManager 시작 완료");
		return true;
	}

	public synchronized void stopModule()
	{
		if(this.isRun)
		{
			this.isRun = false;
			this.commManageThread.interrupt();
		}
		this._users.clear();
		this.clearObservers();
		this.nowTransaction = null;
		this.nowUserIndex = 0;
		SerialFactory.shutdown();
		logger.log(Level.INFO, "SerialReadManager 종료");
	}
	
	public synchronized boolean addUser(byte id)
	{
		if(this._users.containsKey(id)) return false;
		CommUser user = new CommUser(id);
		this._users.put(id, user);
		return true;
	}
	
	public synchronized void removeUser(byte id)
	{
		CommUser user = this._users.getOrDefault(id, null);
		if(user != null)
		{
			if(this.nowTransaction != null && this.nowTransaction.user == user)
			{
				this.nowTransaction = null;
			}
			this._users.remove(id);
		}
	}
	
	private void commManageTask()
	{
		while(this.isRun)
		{
			try
			{
				Thread.sleep(SERIAL_TRANSACTION_TIMEOUT);
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
		if(!this._users.isEmpty())
		{
			if(this.nowUserIndex >= this._users.size())
			{
				this.nowUserIndex = 0;
			}
			CommUser nowUser = this._users.get(this.nowUserIndex);
			this.nowTransaction = new TransactionUnit(System.currentTimeMillis(), nowUser);
			List<byte[]> packetList = nowUser.popData();
			if(packetList.size() == 0)
			{
				byte[] packet = new byte[2];
				packet[0] = nowUser.ID;
				packet[1] = ProtoDef.SERIAL_PACKET_SEG_NODATASERVER;
				this.writeSerial(packet);
			}
			else
			{
				for(byte[] packet : packetList)
				{
					this.writeSerial(packet);
				}
			}

			++this.nowUserIndex;
		}
	}
	
	private void writeSerial(byte[] data)
	{
		try
		{
			this.serial.write(data);
		}
		catch (IllegalStateException | IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			Thread.sleep(SERIAL_DELAY);
		}
		catch (InterruptedException e){ }
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

		if(receiveData.length > ProtoDef.SERIAL_PACKET_MAXSIZE)
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
		if(command == ProtoDef.SERIAL_PACKET_SEG_NODATACLIENT)
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
