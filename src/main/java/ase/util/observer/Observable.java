package ase.util.observer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Observable<Event>
{

	protected List<Observer<Event>> _observers = new LinkedList<Observer<Event>>();

	public void addObserver(Observer<Event> observer)
	{
		if (observer == null)
		{
			throw new IllegalArgumentException("Tried to add a null observer");
		}
		if (this._observers.contains(observer))
		{
			return;
		}
		this._observers.add(observer);
	}
	
	public void removeObserver(Observer<Event> observer)
	{
		if (observer == null)
		{
			throw new IllegalArgumentException("Tried to remove a null observer");
		}
		
		this._observers.remove(observer);
	}
	
	public int size()
	{
		return this._observers.size();
	}

	public void notifyObservers(Event event)
	{
		for (Observer<Event> obs : this._observers)
		{
			obs.update(event);
		}
	}
	
	public void notifyObservers(ExecutorService pool, Event event)
	{
		for (Observer<Event> obs : this._observers)
		{
			pool.submit(()->{obs.update(event);});
		}
	}
	
	public void clearObservers()
	{
		this._observers.clear();
	}
}