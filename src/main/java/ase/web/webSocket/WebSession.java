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
	private final List<WebChannel> _channelList;
	public final List<WebChannel> channelList;
	private ArrayList<WebChannel> controlChList;
	private Observable<ChannelEvent> channelObservable;
	private Consumer<WebSession> closeCallback;
	
	private Timer closeTimer;
	private boolean isActive;
	
	public WebSession(UUID sessionUID, SessionConfigAccess config, Consumer<WebSession> closeCallback)
	{
		this.sessionUID = sessionUID;
		this.sessionConfigAccess = config;
		this._channelList = new ArrayList<>();
		this.controlChList = new ArrayList<>();
		this.channelList = Collections.unmodifiableList(this._channelList);
		this.channelObservable = new Observable<>();
		this.isActive = true;
		this.closeCallback = closeCallback;
	}
	
	public synchronized void onCreateChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		if(ch.getKey().equals(WebSessionManager.CHKEY_CONTROLCH))
		{
			System.out.println("제어 채널 추가");
			ch.assignSession(this);
			if(this.controlChList.size() == 0)
			{
				this.killCloseTimer();
			}
			this.controlChList.add(ch);
		}
		else
		{
			this._channelList.add(ch);
			this.channelObservable.notifyObservers(new ChannelEvent(ch, true));
		}
	}
	
	public synchronized void onCloseChannel(WebChannel ch)
	{
		if(!this.isActive) return;
		if(ch.getKey().equals(WebSessionManager.CHKEY_CONTROLCH))
		{
			System.out.println("제어 채널 삭제");
			this.controlChList.remove(ch);
			if(this.controlChList.size() == 0)
			{
				this.startCloseTimer();
			}
		}
		else
		{
			this.channelObservable.notifyObservers(new ChannelEvent(ch, false));
			this._channelList.remove(ch);
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
		this.closeCallback.accept(this);
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
