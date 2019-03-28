package ase.web.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;

import ase.console.LogWriter;
import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.WebManager;
import ase.web.webSocket.ChannelEvent;
import ase.web.webSocket.WebSocketHandler;

public class SessionManager extends Observable<SessionEvent>
{
	private static final Logger logger = LogWriter.createLogger(SessionManager.class, "SessionManager");
	
	private final WebSocketHandler wsHandler;
	private final SessionConfigAccess sessionConfigAccess;
	private final Observer<ChannelEvent> channelObserver;
	private final Consumer<Session> sessionCloseCallback;
	private final Map<IHTTPSession, Session> sessionMap;
	
	public SessionManager(WebSocketHandler wsHandler)
	{
		this.wsHandler = wsHandler;
		this.channelObserver = this::channelObserver;
		this.sessionConfigAccess = new SessionConfigAccess();
		this.sessionCloseCallback = this::sessionCloseCallback;
		this.sessionMap = new HashMap<>();
	}
	
	private synchronized void channelObserver(Observable<ChannelEvent> provider, ChannelEvent e)
	{
		if(e.isOpen)
		{
			Session session = this.sessionMap.getOrDefault(e.channel.getHandshakeRequest(), null);
			if(session == null)
			{
				session = new Session(e.channel.getHandshakeRequest(), this.sessionConfigAccess, this.sessionCloseCallback);
				this.sessionMap.put(e.channel.getHandshakeRequest(), session);
				this.notifyObservers(new SessionEvent(session, true));
			}
			session.OnCreateChannel(e.channel);
		}
		else
		{
			Session session = this.sessionMap.getOrDefault(e.channel.getHandshakeRequest(), null);
			if(session == null)
			{
				logger.log(Level.WARNING, "관리되지 않는 세션으로부터 채널 종료가 들어옴");
				return;
			}
			session.OnCloseChannel(e.channel);
		}
	}
	
	private synchronized void sessionCloseCallback(Session session)
	{
		this.sessionMap.remove(session.httpSession);
		this.notifyObservers(new SessionEvent(session, false));
	}
	
	public boolean startModule()
	{
		this.sessionConfigAccess.updateFromConfig();
		this.wsHandler.addChannelObserver(this.channelObserver);
		return true;
	}

	public void stopModule()
	{
		this.wsHandler.removeChannelObserver(this.channelObserver);
		for(Session session : this.sessionMap.values())
		{
			session.close();
		}
	}
}
