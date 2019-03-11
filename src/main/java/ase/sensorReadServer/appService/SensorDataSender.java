package ase.sensorReadServer.appService;

import java.nio.ByteBuffer;

import ase.appConnect.channel.AppDataPacketBuilder;
import ase.appConnect.channel.Channel;
import ase.appConnect.channel.ChannelReceiveCallback;
import ase.appConnect.channel.ProtocolDefine;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.DataReceiveEvent;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.sensorReadServer.sensorManager.sensor.SensorData;
import ase.sensorReadServer.sensorManager.sensor.SensorLog;
import ase.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorDataSender implements ChannelReceiveCallback
{
	private Channel channel;
	private SensorManager sensorManager;
	private Sensor sensor;
	
	private Observer<DataReceiveEvent> dataReceiveObserver;
	private Observer<SensorLog> sensorLogObserver;
	private Observer<SensorOnlineEvent> sensorOnlineObserver;
	
	SensorDataSender(Channel channel, SensorManager sensorManager)
	{
		this.channel = channel;
		this.sensorManager = sensorManager;
		
		this.dataReceiveObserver = this::dataReceiveCallback;
		this.sensorLogObserver = this::logReceiveCallback;
		this.sensorOnlineObserver = this::onlineEventCallback;
		
		this.channel.setReceiveCallback(this);
	}
	
	public void destroy()
	{
		if(this.channel.isOpen()) this.channel.setReceiveCallback(null);
		if(this.sensor != null)
		{
			this.sensor.dataReceiveObservable.removeObserver(this.dataReceiveObserver);
			this.sensor.sensorLogObservable.removeObserver(this.sensorLogObserver);
			this.sensor.sensorOnlineObservable.removeObserver(this.sensorOnlineObserver);
		}
			
	}

	@Override
	public void receiveData(Channel ch, byte[][] data)
	{
		switch(data[0][0])
		{
		case AppServiceDefine.SensorData_REQ_DEVICEID:
			this.reqDeviceIDTask(ch, data);
			break;
		case AppServiceDefine.SensorData_REQ_ALLDATA:
			this.sendAllSensorDataTask(data);
			break;
		case AppServiceDefine.SensorData_REQ_ALLLOG:
			this.sendAllLogDataTask(data);
			break;
		}
	}

	public void dataReceiveCallback(Observable<DataReceiveEvent> object, DataReceiveEvent e)
	{
		if(this.sensor == null) return;
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorData_REP_REALTIMEDATA);
			ByteBuffer buf = ByteBuffer.allocate(AppServiceDefine.DATE_FORMAT_SIZE+8+4+4+4+4+4+4);
			buf.put(AppServiceDefine.DATE_FORMAT.format(e.data.time).getBytes());
			buf.putFloat(e.data.X_GRADIANT);
			buf.putFloat(e.data.Y_GRADIANT);
			buf.putFloat(e.data.X_ACCEL);
			buf.putFloat(e.data.Y_ACCEL);
			buf.putFloat(e.data.Z_ACCEL);
			buf.putFloat(e.data.Altitiude);
			b.appendData(buf.array());
			this.channel.sendData(b);
		});
		
		t.setDaemon(true);
		t.start();
	}
	
	public void logReceiveCallback(Observable<SensorLog> object, SensorLog l)
	{
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorData_REP_REALTIMELOG);
			byte[] msg = l.message.getBytes();
			ByteBuffer buf = ByteBuffer.allocate(4 + AppServiceDefine.DATE_FORMAT_SIZE+msg.length);
			buf.putInt(l.level.intValue());
			buf.put(AppServiceDefine.DATE_FORMAT.format(l.time).getBytes());
			buf.put(msg);
			b.appendData(buf.array());
			this.channel.sendData(b);
		});
		t.setDaemon(true);
		t.start();
	}
	
	public void onlineEventCallback(Observable<SensorOnlineEvent> object, SensorOnlineEvent e)
	{
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorData_REP_REALTIMEONOFF);
			b.appendData((byte)(e.isOnline ? 1 : 0));
			this.channel.sendData(b);
		});
		t.setDaemon(true);
		t.start();
	}
	
	private void reqDeviceIDTask(Channel ch, byte[][] data)
	{
		ByteBuffer buf = ByteBuffer.wrap(data[1]);
		int id = buf.getInt();
		this.sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		boolean isValid = this.sensor != null ? true : false;
		if(isValid)
		{
			this.sensor.dataReceiveObservable.addObserver(this.dataReceiveObserver);
			this.sensor.sensorLogObservable.addObserver(this.sensorLogObserver);
			this.sensor.sensorOnlineObservable.addObserver(this.sensorOnlineObserver);
		}
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorData_REP_DEVICEID);
			b.appendData((byte)(isValid ? 1 : 0));
			b.appendData((byte)(isValid && this.sensor.isOnline() ? 1 : 0));
			this.channel.sendData(b);
		});
		t.setDaemon(true);
		t.start();
	}
	
	private void sendAllLogDataTask(byte[][] data)
	{
		int size = ByteBuffer.wrap(data[1]).getInt();
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_REP_ALLLOG);
		int sendStart = this.sensor.log.size() - size;
		if(sendStart < 0) sendStart = 0;
		b.appendData(ProtocolDefine.intToByteArray(this.sensor.log.size() - sendStart));
		for(int i = sendStart; i < this.sensor.log.size(); ++i)
		{
			SensorLog l = this.sensor.log.get(i);
			byte[] msg = l.message.getBytes();
			ByteBuffer buf = ByteBuffer.allocate(4 + AppServiceDefine.DATE_FORMAT_SIZE+msg.length);
			buf.putInt(l.level.intValue());
			buf.put(AppServiceDefine.DATE_FORMAT.format(l.time).getBytes());
			buf.put(msg);
			b.appendData(buf.array());
		}
		this.channel.sendData(b);
	}
	
	private void sendAllSensorDataTask(byte[][] data)
	{
		int size = ByteBuffer.wrap(data[1]).getInt();
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_REP_ALLDATA);
		int sendStart = this.sensor.data.size() - size;
		if(sendStart < 0) sendStart = 0;
		b.appendData(ProtocolDefine.intToByteArray(this.sensor.data.size() - sendStart));
		for(int i = sendStart; i < this.sensor.data.size(); ++i)
		{
			SensorData d = this.sensor.data.get(i);
			ByteBuffer buf = ByteBuffer.allocate(AppServiceDefine.DATE_FORMAT_SIZE+4+4+4+4+4+4);
			buf.put(AppServiceDefine.DATE_FORMAT.format(d.time).getBytes());
			buf.putFloat(d.X_GRADIANT);
			buf.putFloat(d.Y_GRADIANT);
			buf.putFloat(d.X_ACCEL);
			buf.putFloat(d.Y_ACCEL);
			buf.putFloat(d.Z_ACCEL);
			buf.putFloat(d.Altitiude);
			b.appendData(buf.array());
		}
		this.channel.sendData(b);
	}
}
