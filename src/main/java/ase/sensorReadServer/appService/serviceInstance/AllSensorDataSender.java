package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;

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
}
