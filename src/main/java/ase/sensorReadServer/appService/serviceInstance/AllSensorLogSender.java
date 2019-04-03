package ase.sensorReadServer.appService.serviceInstance;

import java.util.function.Consumer;

import ase.clientSession.IChannel;

public class AllSensorLogSender extends ServiceInstance
{
	public static final String KEY = "AllSensorLogRequest";
	
	public AllSensorLogSender(Consumer<ServiceInstance> destoryCallback, IChannel channel)
	{
		super(KEY, destoryCallback, channel);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onStartService()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		
	}


}
