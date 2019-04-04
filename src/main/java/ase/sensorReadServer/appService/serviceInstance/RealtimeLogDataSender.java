package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.IChannel;

public class RealtimeLogDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeLogDataRequest";
	
	public RealtimeLogDataSender(IChannel channel)
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
