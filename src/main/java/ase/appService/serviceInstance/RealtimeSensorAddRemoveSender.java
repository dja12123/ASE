package ase.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeSensorAddRemoveSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorAddRemoveRequest";
	private SensorManager sensorManager;
	private Observer<SensorRegisterEvent> sensorRegisterEventObserver;
	
	public RealtimeSensorAddRemoveSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensorRegisterEventObserver = this::sensorRegisterEventObserver;
	}

	@Override
	protected void onStartService()
	{
		this.sensorManager.addObserver(this.sensorRegisterEventObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.sensorManager.removeObserver(this.sensorRegisterEventObserver);
	}

	@Override
	protected void onDataReceive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		
	}
	
	private void sensorRegisterEventObserver(Observable<SensorRegisterEvent> provider, SensorRegisterEvent event)
	{
		this.channel.sendData(event.sensor.id+"/"+event.isActive);
	}

}