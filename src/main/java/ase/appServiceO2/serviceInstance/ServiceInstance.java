package ase.appServiceO2.serviceInstance;

import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.util.observer.Observer;

public abstract class ServiceInstance
{
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SSS");
	
	public final String key;
	public final IChannel channel;
	private Observer<ChannelDataEvent> onDataRecieve;
	private Consumer<ServiceInstance> destroyCallback;
	
	public ServiceInstance(String key, IChannel channel)
	{
		this.key = key;
		this.channel = channel;
		this.onDataRecieve = this::onDataReceive;
	}
	
	public final void startService(Consumer<ServiceInstance> destroyCallback)
	{
		this.destroyCallback = destroyCallback;
		this.channel.addDataReceiveObserver(this.onDataRecieve);
		this.onStartService();
	}
	
	protected abstract void onStartService();
	
	public final void destroy()
	{
		this.channel.removeDataReceiveObserver(this.onDataRecieve);
		this.onDestroy();
		if(this.destroyCallback != null) this.destroyCallback.accept(this);
	}
	
	protected abstract void onDestroy();
	
	protected abstract void onDataReceive(ChannelDataEvent event);
}
