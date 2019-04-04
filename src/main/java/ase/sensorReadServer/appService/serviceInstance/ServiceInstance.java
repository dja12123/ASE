package ase.sensorReadServer.appService.serviceInstance;

import ase.clientSession.IChannel;

public abstract class ServiceInstance
{
	public final String key;
	protected final IChannel channel;
	
	public ServiceInstance(String key, IChannel channel)
	{
		this.key = key;
		this.channel = channel;
	}
	
	public final void startService()
	{
		this.onStartService();
	}
	
	protected abstract void onStartService();
	
	public final void destroy()
	{
		this.onDestroy();
	}
	
	protected abstract void onDestroy();
}
