package ase.web.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;

import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.webSocket.ChannelEvent;
import ase.web.webSocket.WebSocketChannel;
import ase.web.webSocket.WebSocketHandler;

public class Session extends Observable<ChannelEvent>
{
	public final IHTTPSession httpSession;
	private final SessionConfigAccess sessionConfigAccess;
	private final Consumer<Session> sessionCloseCallback;
	private final List<WebSocketChannel> wsChannelList;
	
	private Timer closeTimer;
	private TimerTask closeTimerTask;
	private boolean isActive;
	
	public Session(IHTTPSession httpSession, SessionConfigAccess config, Consumer<Session> sessionCloseCallback)
	{
		this.httpSession = httpSession;
		this.sessionConfigAccess = config;
		this.sessionCloseCallback = sessionCloseCallback;
		this.wsChannelList = new ArrayList<>();
		this.closeTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				Session.this.close();
			}
		};
		this.isActive = true;
	}
	
	public synchronized void OnCreateChannel(WebSocketChannel ch)
	{
		if(!this.isActive) return;
		if(this.wsChannelList.size() == 0) this.killCloseTimer();
		this.wsChannelList.add(ch);
		this.notifyObservers(new ChannelEvent(ch, true));
	}
	
	public synchronized void OnCloseChannel(WebSocketChannel ch)
	{
		if(!this.isActive) return;
		this.wsChannelList.remove(ch);
		this.notifyObservers(new ChannelEvent(ch, false));
		if(this.wsChannelList.size() == 0) this.startCloseTimer();
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
		for(WebSocketChannel ch : this.wsChannelList)
		{
			ch.normalClose();
		}
		this.isActive = false;
		this.sessionCloseCallback.accept(this);
	}
	
}
