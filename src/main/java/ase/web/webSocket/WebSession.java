package ase.web.webSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.nanohttpd.protocols.http.IHTTPSession;

import ase.clientSession.ChannelEvent;
import ase.clientSession.ISession;
import ase.clientSession.SessionConfigAccess;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class WebSession implements ISession
{
	public final IHTTPSession subLayerSession;
	private final SessionConfigAccess sessionConfigAccess;
	private final Consumer<WebSession> sessionCloseCallback;
	private final List<WebChannel> channelList;
	private Observable<ChannelEvent> channelObservable;
	
	private Timer closeTimer;
	private TimerTask closeTimerTask;
	private boolean isActive;
	
	public WebSession(IHTTPSession subLayerSession, SessionConfigAccess config, Consumer<WebSession> sessionCloseCallback)
	{
		this.subLayerSession = subLayerSession;
		this.sessionConfigAccess = config;
		this.sessionCloseCallback = sessionCloseCallback;
		this.channelList = new ArrayList<>();
		this.channelObservable = new Observable<ChannelEvent>();
		this.closeTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				WebSession.this.close();
			}
		};
		this.isActive = true;
	}
	
	public synchronized void onCreateChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		if(this.channelList.size() == 0) this.killCloseTimer();
		this.channelList.add(ch);
		this.channelObservable.notifyObservers(new ChannelEvent(ch, true));
	}
	
	public synchronized void onCloseChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		this.channelList.remove(ch);
		this.channelObservable.notifyObservers(new ChannelEvent(ch, false));
		if(this.channelList.size() == 0) this.startCloseTimer();
	}
	
	private synchronized void startCloseTimer()
	{
		if(this.closeTimer != null)
		{
			this.closeTimer.cancel();
		}
		this.closeTimer = new Timer();
		this.closeTimer.schedule(this.closeTimerTask, this.sessionConfigAccess.getSessionTimeout());
		
	}
	
	private synchronized void killCloseTimer()
	{
		if(!this.isActive) return;
		if(this.closeTimer != null) this.closeTimer.cancel();
	}
	
	public synchronized void close()
	{
		if(!this.isActive) return;
		for(WebChannel ch : this.channelList)
		{
			ch.close();
		}
		this.isActive = false;
		this.sessionCloseCallback.accept(this);
	}

	@Override
	public void addChannelObserver(Observer<ChannelEvent> observer)
	{
		this.channelObservable.addObserver(observer);
	}

	@Override
	public void removeChannelObserver(Observer<ChannelEvent> observer)
	{
		this.channelObservable.removeObserver(observer);
	}
	
}
