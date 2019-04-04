package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.util.observer.Observable;

public class AllSensorDataSender extends ServiceInstance
{
	public static final String KEY = "AllSensorDataRequest";
	
	private final SensorManager sensorManager;
	
	public AllSensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
	}
	
	@Override
	protected void onDestroy()
	{
		
	}

	@Override
	protected void onStartService()
	{
		
	}

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		// TODO Auto-generated method stub
		
	}
}
