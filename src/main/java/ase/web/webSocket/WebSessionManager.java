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

import com.google.gson.JsonObject;

import ase.clientSession.SessionEvent;
import ase.console.LogWriter;
import ase.util.observer.Observable;
import ase.util.observer.Observer;
import ase.web.httpServer.HTTPServer;

public class WebSessionManager extends Observable<SessionEvent>
{
	private static final Logger logger = LogWriter.createLogger(WebSessionManager.class, "SessionManager");
	public static final String COOKIE_KEY_SESSION = "sessionUUID";
	public static final String CHKEY_CONTROLCH = "control";
	
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
		if(sessionUIDStr == null || !this.sessionMap.containsKey(UUID.fromString(sessionUIDStr)))
		{
			if(e.isOpen && e.channel.getKey().equals(CHKEY_CONTROLCH))
			{
				this.newRequest(e.channel);
				return;
			}
			logger.log(Level.WARNING, "확인되지 않은 채널:"+e.channel.toString());
			return;
		}

		UUID sessionUID = UUID.fromString(sessionUIDStr);
		WebSession session = this._sessionMap.get(sessionUID);
		if(e.isOpen && e.channel.getKey().equals(CHKEY_CONTROLCH))
		{
			logger.log(Level.WARNING, "확인된 채널:"+e.channel.toString());
			this.sendControlMessage(e.channel, session);
		}
		this.requestService(request, session, e.channel, e.isOpen);
	
	}
	
	private void newRequest(WebChannel ch)
	{
		UUID newUUID = UUID.randomUUID();
		WebSession session = new WebSession(newUUID, this.sessionConfigAccess, this.sessionCloseCallback);
		this._sessionMap.put(newUUID, session);
		this.sendControlMessage(ch, session);
		this.notifyObservers(new SessionEvent(session, true));
		session.onCreateChannel(ch);
		logger.log(Level.INFO, "세션 수립:"+session.toString());
	}
	
	private void sendControlMessage(WebChannel ch, WebSession session)
	{
		JsonObject json = new JsonObject();
		json.addProperty("cmdType", "setUUID");
		json.addProperty("sessionUUID", session.sessionUID.toString());
		ch.sendData(json.toString());
	}
	
	private void requestService(IHTTPSession request, WebSession session, WebChannel channel, boolean isOpen)
	{
		if(isOpen) session.onCreateChannel(channel);
		else session.onCloseChannel(channel);
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
