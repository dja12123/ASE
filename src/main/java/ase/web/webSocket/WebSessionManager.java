package ase.web.webSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;

import ase.clientSession.SessionEvent;
import ase.console.LogWriter;
import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.WebManager;

public class WebSessionManager extends Observable<SessionEvent>
{
	private static final Logger logger = LogWriter.createLogger(WebSessionManager.class, "SessionManager");
	
	private final Observable<WebChannelEvent> channelProvider;
	private final SessionConfigAccess sessionConfigAccess;
	private final Observer<WebChannelEvent> channelObserver;
	private final Consumer<WebSession> sessionCloseCallback;
	private final Map<IHTTPSession, WebSession> _sessionMap;
	public final Map<IHTTPSession, WebSession> sessionMap;
	
	public WebSessionManager(Observable<WebChannelEvent> channelProvider)
	{
		this.channelProvider = channelProvider;
		this.channelObserver = this::channelObserver;
		this.sessionConfigAccess = new SessionConfigAccess();
		this.sessionCloseCallback = this::sessionCloseCallback;
		this._sessionMap = new HashMap<>();
		this.sessionMap = Collections.unmodifiableMap(this._sessionMap);
	}
	
	private synchronized void channelObserver(Observable<WebChannelEvent> provider, WebChannelEvent e)
	{
		System.out.println("웹소켓 이벤트" + e.channel.getHandshakeRequest());
		for(IHTTPSession httpSession : this._sessionMap.keySet())
		{
			if(e.channel.getHandshakeRequest().equals(httpSession))
			{
				System.out.println("같은세션");
			}
		}
		if(e.isOpen)
		{
			WebSession session = this._sessionMap.getOrDefault(e.channel.getHandshakeRequest(), null);
			if(session == null)
			{
				session = new WebSession(e.channel.getHandshakeRequest(), this.sessionConfigAccess, this.sessionCloseCallback);
				this._sessionMap.put(e.channel.getHandshakeRequest(), session);
				this.notifyObservers(new SessionEvent(session, true));
			}
			session.onCreateChannel(e.channel);
		}
		else
		{
			WebSession session = this._sessionMap.getOrDefault(e.channel.getHandshakeRequest(), null);
			if(session == null)
			{
				logger.log(Level.WARNING, "관리되지 않는 세션으로부터 채널 종료가 들어옴");
				return;
			}
			session.onCloseChannel(e.channel);
		}
	}
	
	private synchronized void sessionCloseCallback(WebSession session)
	{
		this._sessionMap.remove(session.subLayerSession);
		this.notifyObservers(new SessionEvent(session, false));
	}
	
	public boolean start()
	{
		this.sessionConfigAccess.updateFromConfig();
		this.channelProvider.addObserver(this.channelObserver);
		return true;
	}

	public void stop()
	{
		this.channelProvider.removeObserver(this.channelObserver);
		List<WebSession> closeList = new ArrayList<>();
		closeList.addAll(this._sessionMap.values());
		for(WebSession session : closeList)
		{
			session.close();
		}
		this._sessionMap.clear();
	}
}
