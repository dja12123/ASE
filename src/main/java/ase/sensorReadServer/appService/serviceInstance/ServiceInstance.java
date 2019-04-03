package ase.sensorReadServer.appService.serviceInstance;

import java.util.function.Consumer;

import ase.clientSession.IChannel;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public abstract class ServiceInstance
{
	public final String key;
	
	private final Consumer<ServiceInstance> destoryCallback;
	private final Observer<IChannel> channelCloseObserver;
	protected final IChannel channel;
	
	public ServiceInstance(String key, Consumer<ServiceInstance> destoryCallback, IChannel channel)
	{
		this.key = key;
		this.destoryCallback = destoryCallback;
		this.channel = channel;
		this.channelCloseObserver = this::channelCloseObserver;
	}
	
	public final void startService()
	{
		this.channel.addChannelCloseObserver(this.channelCloseObserver);
		this.onStartService();
	}
	
	protected abstract void onStartService();
	
	public final void destroy()
	{
		this.destoryCallback.accept(this);
		
		if(this.channel.isOpen())
		{
			this.channel.removeChannelCloseObserver(this.channelCloseObserver);
			this.channel.close();
		}
		
		this.onDestroy();
	}
	
	protected abstract void onDestroy();
	
	private final void channelCloseObserver(Observable<IChannel> provider, IChannel channel)
	{
		this.destroy();
	}
}
