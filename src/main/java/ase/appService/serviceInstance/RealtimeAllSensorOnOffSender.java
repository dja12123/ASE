package ase.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeAllSensorOnOffSender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllSensorOnOffRequest";
	private SensorManager sensorManager;
	private Observer<SensorOnlineEvent> sensorOnlineEventObserver;
	
	public RealtimeAllSensorOnOffSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensorOnlineEventObserver = this::sensorOnlineEventObserver;
	}

	@Override
	protected void onStartService()
	{
		this.sensorManager.publicSensorOnlineObservable.addObserver(this.sensorOnlineEventObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.sensorManager.publicSensorOnlineObservable.removeObserver(this.sensorOnlineEventObserver);
	}

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		
	}
	
	private void sensorOnlineEventObserver(Observable<SensorOnlineEvent> provider, SensorOnlineEvent event)
	{
		this.channel.sendData(event.sensor.id+"/"+event.isOnline);
	}

}
