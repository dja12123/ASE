package ase.clientSession;

import java.util.ArrayList;
import java.util.List;

import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class ClientSessionManager extends Observable<SessionEvent>
{
	private List<Observable<SessionEvent>> sessionEventProviders;
	private Observer<SessionEvent> sessionEventObserver;
	private int sessionCount;
	
	public ClientSessionManager()
	{
		this.sessionEventProviders = new ArrayList<>();
		this.sessionEventObserver = this::sessionEventObserver;
	}
	
	public synchronized void addSessionProvider(Observable<SessionEvent> provider)
	{
		this.sessionEventProviders.add(provider);
	}
	
	public synchronized void removeSessionProvider(Observable<SessionEvent> provider)
	{
		this.sessionEventProviders.remove(provider);
	}
	
	private synchronized void sessionEventObserver(SessionEvent event)
	{
		if(event.isActive) ++this.sessionCount;
		else --this.sessionCount;
		this.notifyObservers(event);
	}
	
	public int getSessionCount()
	{
		return this.sessionCount;
	}
	
	public boolean startModule()
	{
		this.sessionCount = 0;
		for(Observable<SessionEvent> provider : this.sessionEventProviders)
		{
			provider.addObserver(this.sessionEventObserver);
		}
		return true;
	}
	
	public void stopModule()
	{
		for(Observable<SessionEvent> provider : this.sessionEventProviders)
		{
			provider.removeObserver(this.sessionEventObserver);
		}
	}
}