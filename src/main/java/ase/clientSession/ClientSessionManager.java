package ase.clientSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class ClientSessionManager extends Observable<SessionEvent>
{
	private List<Observable<SessionEvent>> _sessionEventProviders;
	public List<Observable<SessionEvent>> sessionEventProviders;
	private Observer<SessionEvent> sessionEventObserver;
	
	public ClientSessionManager()
	{
		this._sessionEventProviders = new ArrayList<>();
		this.sessionEventProviders = Collections.unmodifiableList(this._sessionEventProviders);
		this.sessionEventObserver = this::sessionEventObserver;
	}
	
	public void addSessionProvider(Observable<SessionEvent> provider)
	{
		this._sessionEventProviders.add(provider);
	}
	
	public void removeSessionProvider(Observable<SessionEvent> provider)
	{
		this._sessionEventProviders.remove(provider);
	}
	
	private void sessionEventObserver(SessionEvent event)
	{
		this.notifyObservers(event);
	}
	
	public boolean startModule()
	{
		for(Observable<SessionEvent> provider : this._sessionEventProviders)
		{
			provider.addObserver(this.sessionEventObserver);
		}
		return true;
	}
	
	public void stopModule()
	{
		for(Observable<SessionEvent> provider : this._sessionEventProviders)
		{
			provider.removeObserver(this.sessionEventObserver);
		}
	}
}