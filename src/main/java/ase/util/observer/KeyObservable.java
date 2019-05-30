package ase.util.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class KeyObservable<Key, Event>
{
	private Map<Key, List<KeyObserver<Key, Event>>> _observers;
	
	public KeyObservable()
	{
		this._observers = new HashMap<>();
	}
	
	public void addObserver(Key key, KeyObserver<Key, Event> observer)
	{
		if (observer == null)
		{
			throw new IllegalArgumentException("Tried to add a null observer");
		}
		List<KeyObserver<Key, Event>> observerList = this._observers.getOrDefault(key, null);
		if(observerList == null)
		{
			observerList = new ArrayList<>();
		}
		else if(observerList.contains(observer))
		{
			return;
		}
		this._observers.put(key, observerList);
		
	}
	
	public void removeObserver(Key key, KeyObserver<Key, Event> observer)
	{
		if (observer == null)
		{
			throw new IllegalArgumentException("Tried to remove a null observer");
		}
		List<KeyObserver<Key, Event>> observerList = this._observers.getOrDefault(key, null);
		if(observerList == null)
		{
			return;
		}
		observerList.remove(observer);
		if(observerList.isEmpty())
		{
			this._observers.remove(key);
		}
	}
	
	public int size()
	{
		int size = 0;
		for(List<KeyObserver<Key, Event>> list : this._observers.values())
		{
			size += list.size();
		}
		return size;
	}

	public void notifyObservers(Key key, Event event)
	{
		List<KeyObserver<Key, Event>> list = this._observers.getOrDefault(key, null);
		if(list == null)
		{
			return;
		}
		
		for (KeyObserver<Key, Event> obs : list)
		{
			obs.update(key, event);
		}
	}
	
	public void notifyObservers(ExecutorService pool, Key key, Event event)
	{
		List<KeyObserver<Key, Event>> list = this._observers.getOrDefault(key, null);
		if(list == null)
		{
			System.out.println("ERROR 옵저버없음" + event.toString());
			return;
		}
		
		for (KeyObserver<Key, Event> obs : list)
		{
			pool.submit(()->{obs.update(key, event);});
		}
	}
	
	public void clearObservers()
	{
		this._observers.clear();
	}
}
