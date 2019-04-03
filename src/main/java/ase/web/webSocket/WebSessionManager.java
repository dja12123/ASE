package ase.web.webSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;

import ase.clientSession.SessionEvent;
import ase.console.LogWriter;
import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.httpServer.HTTPServer;

public class WebSessionManager extends Observable<SessionEvent>
{
	private static final Logger logger = LogWriter.createLogger(WebSessionManager.class, "SessionManager");
	public static final String COOKIE_KEY_SESSION = "sessionUID";
	
	private final Observable<WebChannelEvent> channelProvider;
	private final SessionConfigAccess sessionConfigAccess;
	private final Observer<WebChannelEvent> channelObserver;
	private final Consumer<WebSession> sessionCloseCallback;
	private final Map<UUID, WebSession> _sessionMap;
	public final Map<UUID, WebSession> sessionMap;
	
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
		IHTTPSession request = e.channel.getHandshakeRequest();
		String sessionUIDStr = HTTPServer.getCookie(request, COOKIE_KEY_SESSION);
		if(sessionUIDStr == null)
		{
			logger.log(Level.WARNING, "확인되지 않은 채널:"+e.channel.toString());
			return;
		}
		else
		{
			UUID sessionUID = UUID.fromString(sessionUIDStr);
			WebSession session = this._sessionMap.getOrDefault(sessionUID, null);
			if(session != null)
			{
				this.requestService(request, session, e.channel, e.isOpen);
			}
			else
			{
				if(e.isOpen) this.newRequest(sessionUID, request, e.channel);
			}
		}
	}
	
	private void newRequest(UUID sessionUID, IHTTPSession request, WebChannel ch)
	{
		WebSession session = new WebSession(sessionUID, this.sessionConfigAccess, this.sessionCloseCallback);
		this._sessionMap.put(sessionUID, session);
		this.notifyObservers(new SessionEvent(session, true));
		session.onCreateChannel(ch);
		logger.log(Level.INFO, "세션 수립:"+session.toString()+" 채널개수:"+session.channelList.size());
	}
	
	private void requestService(IHTTPSession request, WebSession session, WebChannel channel, boolean isOpen)
	{
		if(isOpen) session.onCreateChannel(channel);
		else session.onCloseChannel(channel);
		logger.log(Level.INFO, "웹소켓 "+isOpen+":"+session.toString()+" 채널개수:"+session.channelList.size());
	}
	
	private synchronized void sessionCloseCallback(WebSession session)
	{
		this._sessionMap.remove(session.sessionUID);
		this.notifyObservers(new SessionEvent(session, false));
		logger.log(Level.INFO, "세션 종료:"+session.toString());
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
