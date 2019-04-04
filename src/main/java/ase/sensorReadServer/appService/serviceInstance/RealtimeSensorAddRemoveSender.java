package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.util.observer.Observable;

public class RealtimeSensorAddRemoveSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorAddRemoveRequest";
	
	public RealtimeSensorAddRemoveSender(IChannel channel)
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

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		// TODO Auto-generated method stub
		
	}

}
