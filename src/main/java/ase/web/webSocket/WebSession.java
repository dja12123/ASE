package ase.web.webSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.Consumer;

import ase.clientSession.ChannelEvent;
import ase.clientSession.ISession;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class WebSession implements ISession
{
	public final UUID sessionUID;
	private final SessionConfigAccess sessionConfigAccess;
	private final Consumer<WebSession> sessionCloseCallback;
	private final List<WebChannel> _channelList;
	public final List<WebChannel> channelList;
	private Observable<ChannelEvent> channelObservable;
	
	private Timer closeTimer;
	private boolean isActive;
	
	public WebSession(UUID sessionUID, SessionConfigAccess config, Consumer<WebSession> sessionCloseCallback)
	{
		this.sessionUID = sessionUID;
		this.sessionConfigAccess = config;
		this.sessionCloseCallback = sessionCloseCallback;
		this._channelList = new ArrayList<>();
		this.channelList = Collections.unmodifiableList(this._channelList);
		this.channelObservable = new Observable<ChannelEvent>();
		this.isActive = true;
	}
	
	public synchronized void onCreateChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		if(this._channelList.size() == 0)
		{
			this.killCloseTimer();
			System.out.println("클로즈 타이머 죽임");
		}
		this._channelList.add(ch);
		this.channelObservable.notifyObservers(new ChannelEvent(ch, true));
	}
	
	public synchronized void onCloseChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		this._channelList.remove(ch);
		this.channelObservable.notifyObservers(new ChannelEvent(ch, false));
		if(this._channelList.size() == 0)
		{
			this.startCloseTimer();
			System.out.println("클로즈 타이머 시작");
		}
	}
	
	private synchronized void startCloseTimer()
	{
		if(this.closeTimer != null)
		{
			this.closeTimer.cancel();
		}
		this.closeTimer = new Timer();
		TimerTask closeTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				WebSession.this.close();
			}
		};
		this.closeTimer.schedule(closeTimerTask, this.sessionConfigAccess.getSessionTimeout());
		
	}
	
	private synchronized void killCloseTimer()
	{
		if(!this.isActive) return;
		if(this.closeTimer != null) this.closeTimer.cancel();
	}
	
	@Override
	public synchronized void close()
	{
		if(!this.isActive) return;
		for(WebChannel ch : this._channelList)
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
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Session UID: ");
		buf.append(this.sessionUID.toString());
		return buf.toString();
		
	}
	
}