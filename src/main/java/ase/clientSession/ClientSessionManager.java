package ase.clientSession;

import java.util.ArrayList;
import java.util.List;

import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class ClientSessionManager extends Observable<SessionEvent>
{
	private List<Observable<SessionEvent>> sessionEventProviders;
	private Observer<SessionEvent> sessionEventObserver;
	
	public ClientSessionManager()
	{
		this.sessionEventProviders = new ArrayList<>();
		this.sessionEventObserver = this::sessionEventObserver;
	}
	
	public void addSessionProvider(Observable<SessionEvent> provider)
	{
		this.sessionEventProviders.add(provider);
	}
	
	public void removeSessionProvider(Observable<SessionEvent> provider)
	{
		this.sessionEventProviders.remove(provider);
	}
	
	private void sessionEventObserver(Observable<SessionEvent> provider, SessionEvent event)
	{
		this.notifyObservers(event);
	}
	
	public boolean startModule()
	{
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