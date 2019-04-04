package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.IChannel;

public class RealtimeSensorDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorDataRequest";
	
	public RealtimeSensorDataSender(IChannel channel)
	{
		super(KEY, channel);
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
