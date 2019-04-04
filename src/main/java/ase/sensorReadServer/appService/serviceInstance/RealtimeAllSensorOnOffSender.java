package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeAllSensorOnOffSender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllSensorOnOffRequest";
	private SensorManager sensorManager;
	private Observer<SensorOnlineEvent> sensorRegisterEventObserver;
	
	public RealtimeAllSensorOnOffSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensorRegisterEventObserver = this::sensorRegisterEventObserver;
	}

	@Override
	protected void onStartService()
	{
		this.sensorManager.publicSensorOnlineObservable.addObserver(this.sensorRegisterEventObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.sensorManager.publicSensorOnlineObservable.removeObserver(this.sensorRegisterEventObserver);
	}

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		
	}
	
	private void sensorRegisterEventObserver(Observable<SensorOnlineEvent> provider, SensorOnlineEvent event)
	{
		this.channel.sendData(event.sensor.id+"/"+event.isOnline);
	}

}
