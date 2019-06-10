package ase.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensorOnline.SensorOnlineCheck;
import ase.sensorManager.sensorOnline.SensorOnlineEvent;
import ase.util.observer.Observer;

public class RealtimeAllSensorOnOffSender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllSensorOnOffRequest";
	private final SensorManager sensorManager;
	private final SensorOnlineCheck onlineCheck;
	private Observer<SensorOnlineEvent> sensorOnlineEventObserver;
	
	public RealtimeAllSensorOnOffSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.onlineCheck = this.sensorManager.sensorOnlineCheck;
		this.sensorOnlineEventObserver = this::sensorOnlineEventObserver;
	}

	@Override
	protected void onStartService()
	{
		this.onlineCheck.addObserver(this.sensorOnlineEventObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.onlineCheck.removeObserver(this.sensorOnlineEventObserver);
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		
	}
	
	private void sensorOnlineEventObserver(SensorOnlineEvent event)
	{
		this.channel.sendData(event.sensor.ID+"/"+event.isOnline);
	}

}
