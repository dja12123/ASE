package ase.sensorReadServer.appService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.SensorRegisterEvent;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class AllSensorDataSender
{
	private IChannel channel;
	private SensorManager sensorManager;
	private Observer<SensorOnlineEvent> sensorOnlineObserver;
	private Observer<SensorRegisterEvent> sensorRegisterObserver;
	private Observer<ChannelDataEvent> channelDataObserver;
	
	AllSensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		this.channel = channel;
		this.sensorManager = sensorManager;
		this.sensorOnlineObserver = this::sensorOnlineCallback;
		this.sensorRegisterObserver = this::sensorRegisterCallback;
		this.channelDataObserver = this::channelDataObserver;
		this.sensorManager.publicSensorOnlineObservable.addObserver(this.sensorOnlineObserver);
		this.sensorManager.addObserver(this.sensorRegisterObserver);
		this.channel.addDataReceiveObserver(this.channelDataObserver);
	}
	
	public void destroy()
	{
		this.channel.removeDataReceiveObserver(this.channelDataObserver);
		this.sensorManager.publicSensorOnlineObservable.removeObserver(this.sensorOnlineObserver);
		this.sensorManager.removeObserver(this.sensorRegisterObserver);
	}

	public void channelDataObserver(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		if(event.data[0] == AppServiceDefine.SensorDeviceData_REQ_LIST)
		{
			this.sendAllSensorDataTask();
		}
	}
	
	private void sendAllSensorDataTask()
	{
		List<Sensor> sensorList = new ArrayList<>(this.sensorManager.sensorMap.size());
		sensorList.addAll(this.sensorManager.sensorMap.values());
		ByteBuffer buf = ByteBuffer.allocate(1+4+(sensorList.size()*(4 + 1)));
		buf.put(AppServiceDefine.SensorDeviceData_REP_LIST);
		buf.putInt(sensorList.size());
		for(Sensor sensor : sensorList)
		{
			buf.putInt(sensor.id);
			buf.put((byte)(sensor.isOnline() ? 1 : 0));
		}
		this.channel.sendData(buf.array());
	}

	public void sensorOnlineCallback(Observable<SensorOnlineEvent> object, SensorOnlineEvent data)
	{
		ByteBuffer buf = ByteBuffer.allocate(1+4+1);
		buf.put(AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ONOFF);
		buf.putInt(data.sensor.id);
		buf.put((byte)(data.isOnline ? 1 : 0));
		this.channel.sendData(buf.array());
	}
	
	public void sensorRegisterCallback(Observable<SensorRegisterEvent> object, SensorRegisterEvent data)
	{	
		ByteBuffer buf = ByteBuffer.allocate(1+4+1);
		buf.put(AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ADDREMOVE);
		buf.putInt(data.sensor.id);
		buf.put((byte)(data.isActive ? 1 : 0));
		this.channel.sendData(buf.array());
	}
}
