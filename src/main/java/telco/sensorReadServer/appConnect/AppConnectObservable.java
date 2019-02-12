package telco.sensorReadServer.appConnect;

import java.util.ArrayList;
import java.util.HashMap;

import telco.sensorReadServer.appConnect.protocol.Channel;
import telco.sensorReadServer.appConnect.protocol.Connection;
import telco.sensorReadServer.appConnect.protocol.ConnectionUser;
import telco.sensorReadServer.util.observer.Observable;
import telco.sensorReadServer.util.observer.Observer;

public class AppConnectObservable implements ConnectionUser
{	
	private HashMap<String, Observable<AppDataReceiveEvent>> dataObserverMap;
	private Observable<ConnectionStateChangeEvent> connectionStateChangeObservable;
	
	public AppConnectObservable()
	{
		this.dataObserverMap = new HashMap<String, Observable<AppDataReceiveEvent>>();
		this.connectionStateChangeObservable = new Observable<ConnectionStateChangeEvent>();
	}
	
	public void addDataReceiveObserver(String key, Observer<AppDataReceiveEvent> observer)
	{
		Observable<AppDataReceiveEvent> ob = this.dataObserverMap.getOrDefault(key, null);
		if(ob == null)
		{
			ob = (new Observable<AppDataReceiveEvent>());
			this.dataObserverMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeDataReceiveObserver(String key, Observer<AppDataReceiveEvent> observer)
	{
		Observable<AppDataReceiveEvent> observable = this.dataObserverMap.getOrDefault(key, null);
		if(observable == null)
		{
			return;
		}
		observable.removeObserver(observer);
		
		if(observable.size() == 0)
		{
			this.dataObserverMap.remove(key);
		}
	}
	
	public void removeDataReceiveObserver(Observer<AppDataReceiveEvent> observer)
	{
		Observable<AppDataReceiveEvent> observable;
		ArrayList<String> removeObservableKey = new ArrayList<>();
		for(String key : this.dataObserverMap.keySet())
		{
			observable = this.dataObserverMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
			{
				removeObservableKey.add(key);
			}
		}
		
		for(int i = 0; i < removeObservableKey.size(); ++i)
		{
			this.dataObserverMap.remove(removeObservableKey.get(i));
		}
	}
	
	public void addConnectionStateChangeObserver(Observer<ConnectionStateChangeEvent> observer)
	{
		this.connectionStateChangeObservable.addObserver(observer);
	}
	
	public void removeConnectionStateChangeObserver(Observer<ConnectionStateChangeEvent> observer)
	{
		this.connectionStateChangeObservable.removeObserver(observer);
	}
	
	public void clearObservers()
	{
		this.dataObserverMap.clear();
		this.connectionStateChangeObservable.clearObservers();
	}

	@Override
	public void createChannel(Connection connection, Channel channel)
	{
		Observable<AppDataReceiveEvent> observable = this.dataObserverMap.getOrDefault(channel.key, null);
		if(observable == null)
		{
			return;
		}
		AppDataReceiveEvent e = new AppDataReceiveEvent(connection, channel);
		observable.notifyObservers(e);
	}

	@Override
	public void receiveGeneralData(Connection connection, String key, byte[][] data)
	{
		Observable<AppDataReceiveEvent> observable = this.dataObserverMap.getOrDefault(key, null);
		if(observable == null)
		{
			return;
		}
		AppDataReceiveEvent e = new AppDataReceiveEvent(connection, key, data);
		observable.notifyObservers(e);
	}

	@Override
	public void startConnection(Connection connection)
	{
		ConnectionStateChangeEvent e = new ConnectionStateChangeEvent(connection, true);
		this.connectionStateChangeObservable.notifyObservers(e);
		
	}

	@Override
	public void closeConnection(Connection connection)
	{
		ConnectionStateChangeEvent e = new ConnectionStateChangeEvent(connection, false);
		this.connectionStateChangeObservable.notifyObservers(e);
	}


	
}
