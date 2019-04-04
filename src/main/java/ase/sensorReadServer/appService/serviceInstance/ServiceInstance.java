package ase.sensorReadServer.appService.serviceInstance;

import java.text.SimpleDateFormat;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public abstract class ServiceInstance
{
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
	
	public final String key;
	protected final IChannel channel;
	private Observer<ChannelDataEvent> onDataRecieve;
	
	public ServiceInstance(String key, IChannel channel)
	{
		this.key = key;
		this.channel = channel;
		this.onDataRecieve = this::onDataRecive;
	}
	
	public final void startService()
	{
		this.channel.addDataReceiveObserver(this.onDataRecieve);
		this.onStartService();
	}
	
	protected abstract void onStartService();
	
	public final void destroy()
	{
		this.channel.removeDataReceiveObserver(this.onDataRecieve);
		this.onDestroy();
	}
	
	protected abstract void onDestroy();
	
	protected abstract void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event);
}
