package ase.web.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;

import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.webSocket.ChannelEvent;
import ase.web.webSocket.WebSocketChannel;
import ase.web.webSocket.WebSocketHandler;

public class Session
{
	public final IHTTPSession httpSession;
	private WebSocketHandler webSocketHandler;
	private final SessionConfigAccess sessionConfigAccess;
	private Observer<ChannelEvent> channelObserver;
	private List<WebSocketChannel> wsChannelList;
	
	private Timer closeTimer;
	private TimerTask closeTimerTask;
	private boolean isActive;

	
	public Session(IHTTPSession httpSession, WebSocketHandler wsHandler, SessionConfigAccess config)
	{
		this.httpSession = httpSession;
		this.webSocketHandler = wsHandler;
		this.sessionConfigAccess = config;
		this.channelObserver = this::channelObserver;
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
		this.webSocketHandler.channelObservable.addObserver(this.channelObserver);
	}
	
	public synchronized void startCloseTimer()
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
		if(this.isActive) return;
		if(this.closeTimer != null) this.closeTimer.cancel();
	}
	
	public synchronized void close()
	{
		if(!this.isActive) return;
		this.webSocketHandler.removeChannelObserver(this.channelObserver);
		for(WebSocketChannel ch : this.wsChannelList)
		{
			ch.normalClose();
		}
		this.isActive = false;
	}
	
	private synchronized void channelObserver(Observable<ChannelEvent> provider, ChannelEvent e)
	{
		if(!this.isActive) return;
		if(e.channel.getHandshakeRequest() != this.httpSession) return;
		if(e.isOpen)
		{
			this.wsChannelList.add(e.channel);
			this.killCloseTimer();
		}
		else
		{
			this.wsChannelList.remove(e.channel);
			this.startCloseTimer();
		}
	}
}
