package ase.sensorComm.protocolSerial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
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
import ase.sensorComm.CommOnlineEvent;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.ReceiveEvent;
import ase.util.observer.KeyObservable;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class ProtocolSerial extends KeyObservable<Short, ReceiveEvent> implements ISensorCommManager
{
	public static final Logger logger = LogWriter.createLogger(ProtocolSerial.class, "protoSerialReader");
	public static final String PROP_SerialDevice = "SerialDevice";
	public static final String PROP_SerialRate = "SerialRate";
	
	private final Observable<CommOnlineEvent> onlineObservable;
	private final Serial serial;
	private final SerialConfig config;
	private final SerialWriter serialWriter;
	private final Queue<byte[]> broadcastPacket;
	
	private boolean isRun;
	
	private Thread commManageThread;
	private final Runnable commManageTask;
	
	private final Map<Integer, SerialTransmitter> _users;
	private final List<SerialTransmitter> _userList;
	private final Map<Integer, SerialTransmitter> users;
	
	private SerialReceiver nowTransaction;
	private int nowUserIndex;

	public ProtocolSerial()
	{
		this.onlineObservable = new Observable<>();
		this.serial = SerialFactory.createInstance();
		this.serial.addListener(this::dataReceived);
		this.config = new SerialConfig();
		this.config.baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);
		this.serialWriter = new SerialWriter(this.serial);
		this.broadcastPacket = new LinkedBlockingQueue<>();
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
		this.broadcastPacket.clear();
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
		if(intid > Byte.MAX_VALUE || intid == ProtoDef.SERIAL_PACKET_BROADCAST_ADDR)
		{
			return false;
		}
		byte id = (byte)intid;
		if(this._users.containsKey(intid)) return false;
		SerialTransmitter user = new SerialTransmitter(id);
		this._users.put(intid, user);
		this._userList.add(user);
		return true;
	}
	
	@Override
	public synchronized void removeUser(int intid)
	{
		byte id = (byte)intid;
		SerialTransmitter user = this._users.getOrDefault(id, null);
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
	public Map<Integer, SerialTransmitter> getUserMap()
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
					if(this.nowTransaction.user.isOnline())
					{
						logger.log(Level.WARNING, "센서 오프라인 " + this.nowTransaction.user.ID);
						this.nowTransaction.user.setOnline(false);
						CommOnlineEvent onlineEvent = new CommOnlineEvent(this.nowTransaction.user.ID, false);
						this.onlineObservable.notifyObservers(onlineEvent);
					}
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
			SerialTransmitter nowUser = this._userList.get(this.nowUserIndex);
			this.nowTransaction = new SerialReceiver(System.currentTimeMillis(), nowUser);
			List<byte[]> packetList = nowUser.popData();
			if(packetList.size() == 0)
			{
				byte[] packet = new byte[SerialProtoDef.SERIAL_PACKET_HEADERSIZE];
				packet[0] = 3;
				packet[1] = nowUser.ID;
				packet[2] = SerialProtoDef.SERIAL_PACKET_SEG_NODATASERVER;
				this.serialWriter.write(packet);
			}
			else
			{
				for(int i = 0; i < packetList.size() - 1; ++i)
				{
					this.serialWriter.write(packetList.get(i));
				}
				if(!packetList.isEmpty())
				{
					byte[] packet = packetList.get(packetList.size() - 1);
					packet[2] = SerialProtoDef.SERIAL_PACKET_SEG_ENDFROMSERVER;
					this.serialWriter.write(packet);
				}
			}

			++this.nowUserIndex;
		}
	}
	
	
	private synchronized void dataReceived(SerialDataEvent event)
	{
		byte[] packet;
		try
		{
			packet = event.getBytes();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		if(this.nowTransaction == null)
		{
			return;
		}
		
		int nowIndex = 0;
		while(nowIndex < packet.length)
		{
			byte packetSize = packet[nowIndex];
			if(packetSize > ProtoDef.PACKET_MAXSIZE)
			{
				return;
			}
			byte[] splitPacket = new byte[packetSize];
			System.arraycopy(packet, nowIndex, splitPacket, 0, packetSize);
			if(!this.nowTransaction.putReceiveData(splitPacket))
			{
				return;
			}
			logger.log(Level.INFO, "패킷길이: " +splitPacket.length);
			if(this.nowTransaction.isReceiveFinish())
			{
				for(byte[] data : this.nowTransaction.getReceiveData())
				{
					ByteBuffer buf = ByteBuffer.wrap(data);
					buf.position(SerialProtoDef.SERIAL_PACKET_HEADERSIZE);
					short key = buf.getShort();
					byte[] value = new byte[data.length - SerialProtoDef.SERIAL_PACKET_HEADERSIZE - ProtoDef.SERIAL_PACKET_KEYSIZE];
					buf.get(value);
					ReceiveEvent e = new ReceiveEvent(this.nowTransaction.user.ID, key, value);
					this.notifyObservers(ServerCore.mainThreadPool, e.key, e);
				}
				if(!this.nowTransaction.user.isOnline())
				{
					logger.log(Level.INFO, "센서 온라인 " + this.nowTransaction.user.ID);
					this.nowTransaction.user.setOnline(true);
					CommOnlineEvent onlineEvent = new CommOnlineEvent(this.nowTransaction.user.ID, true);
					this.onlineObservable.notifyObservers(onlineEvent);
				}
				this.nowTransaction = null;
			}
			nowIndex += packetSize;
		}
		
		
		
		this.commManageThread.interrupt();
	}

	@Override
	public void addOnlineObserver(Observer<CommOnlineEvent> observer)
	{
		this.onlineObservable.addObserver(observer);
		
	}

	@Override
	public void removeOnlineObserver(Observer<CommOnlineEvent> observer)
	{
		this.onlineObservable.removeObserver(observer);
	}

	@Override
	public void sendBroadcast(short key, byte[] value)
	{
		if(!this.isRun) return;
		ByteBuffer buf = ByteBuffer.allocate(SerialProtoDef.SERIAL_PACKET_HEADERSIZE + ProtoDef.SERIAL_PACKET_KEYSIZE + value.length);
		buf.put((byte)buf.array().length);
		buf.put((byte) ProtoDef.SERIAL_PACKET_BROADCAST_ADDR);
		buf.put(SerialProtoDef.SERIAL_PACKET_SEG_BROADCAST);
		buf.putShort(key);
		buf.put(value);
		this.broadcastPacket.add(buf.array());
	}

}
