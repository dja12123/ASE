package ase.sensorReadServer.appService;

import java.nio.ByteBuffer;

import ase.appConnect.channel.AppDataPacketBuilder;
import ase.appConnect.channel.Channel;
import ase.appConnect.channel.ChannelReceiveCallback;
import ase.appConnect.channel.ProtocolDefine;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.SensorRegisterEvent;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class AllSensorDataSender implements ChannelReceiveCallback
{
	private Channel channel;
	private SensorManager sensorManager;
	private Observer<SensorOnlineEvent> sensorOnlineObserver;
	private Observer<SensorRegisterEvent> sensorRegisterObserver;
	
	AllSensorDataSender(Channel channel, SensorManager sensorManager)
	{
		this.channel = channel;
		this.sensorManager = sensorManager;
		this.sensorOnlineObserver = this::sensorOnlineCallback;
		this.sensorRegisterObserver = this::sensorRegisterCallback;
		this.sensorManager.publicSensorOnlineObservable.addObserver(this.sensorOnlineObserver);
		this.sensorManager.addObserver(this.sensorRegisterObserver);
		this.channel.setReceiveCallback(this);
	}
	
	public void destroy()
	{
		if(this.channel.isOpen()) this.channel.setReceiveCallback(null);
		this.sensorManager.publicSensorOnlineObservable.removeObserver(this.sensorOnlineObserver);
		this.sensorManager.removeObserver(this.sensorRegisterObserver);
	}

	@Override
	public void receiveData(Channel ch, byte[][] data)
	{
		if(data[0][0] == AppServiceDefine.SensorDeviceData_REQ_LIST)
		{
			this.sendAllSensorDataTask();
		}
	}
	
	private void sendAllSensorDataTask()
	{
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorDeviceData_REP_LIST);
		b.appendData(ProtocolDefine.intToByteArray(this.sensorManager.sensorMap.size()));
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			ByteBuffer buf = ByteBuffer.allocate(4 + 1);
			buf.putInt(sensor.id);
			buf.put((byte)(sensor.isOnline() ? 1 : 0));
			b.appendData(buf.array());
		}
		this.channel.sendData(b);
	}
	
	public void sendAllSensorLogTask()
	{
		
	}

	public void sensorOnlineCallback(Observable<SensorOnlineEvent> object, SensorOnlineEvent data)
	{
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ONOFF);
			b.appendData(ProtocolDefine.intToByteArray(data.sensor.id));
			b.appendData((byte)(data.isOnline ? 1 : 0));
			this.channel.sendData(b);
		});
		t.setDaemon(true);
		t.start();
	}
	
	public void sensorRegisterCallback(Observable<SensorRegisterEvent> object, SensorRegisterEvent data)
	{
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ADDREMOVE);
			b.appendData(ProtocolDefine.intToByteArray(data.sensor.id));
			b.appendData((byte)(data.isActive ? 1 : 0));
			this.channel.sendData(b);
		});
		t.setDaemon(true);
		t.start();
	}
}
