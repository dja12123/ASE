package ase.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import ase.appConnect.channel.ChannelReceiveCallback;
import ase.util.observer.Observable;

/*
 * WebSocket의 데이터를 정의한 클래스
 * @extends WebSocket
 */
public class WebSocketChannel extends WebSocket
{
	public static final Logger logger = WebSocketHandler.logger;
	private HashMap<String, Observable<WebEvent>> createWebSocketObserver;
	private ChannelReceiveCallback recvCallback;
	
	public WebSocketChannel(IHTTPSession handshakeRequest, HashMap<String, Observable<WebEvent>> observerMap)
	{
		super(handshakeRequest);
		this.createWebSocketObserver = observerMap;
		
	}
	
	public void setReceiveCallback(ChannelReceiveCallback callback)
	{
		if(!this.isOpen()) return;
		
		this.recvCallback = callback;
	}
	
	@Override
	protected void onOpen() 
	{
		logger.log(Level.INFO, "웹소켓 열림");
	}
	
	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) 
	{
		String logMsg = "웹소켓 닫힘 [" + (initiatedByRemote ? "Remote" : "Self") + "]"
						+ (code != null ? code : "UnknownCloseCode[" + code + "]")
						+ (reason != null && !reason.isEmpty() ? ": " + reason : "");
		logger.log(Level.INFO, logMsg);
	}
	
	@Override
	protected void onMessage(WebSocketFrame frame) 
	{
		String key = frame.getTextPayload();
		WebEvent event = new WebEvent(this, key);
		
		Observable<WebEvent> observable = createWebSocketObserver.get(key);
		
		if (observable == null) 
		{
			logger.log(Level.INFO, "웹 소켓 옵저버가 비어 있음");
			return;
		}

		logger.log(Level.INFO, "observable size >> " + observable.size());
		
		for (int i = 0; i < createWebSocketObserver.size(); ++i)
		{
			logger.log(Level.INFO, i + " >> 옵저버에게 알림: " + key);
			observable.notifyObservers(event);
		}
		
		logger.log(Level.INFO, "보내자!" + frame.toString());
	}
	
	@Override
	protected void onPong(WebSocketFrame pong) 
	{
		logger.log(Level.INFO, "웹 소켓 Pong " + pong);
	}
	
	@Override
	protected void onException(IOException exception)
	{
		logger.log(Level.SEVERE, "웹 소켓 예외가 발생함", exception);
	}
	
	@Override
	protected void debugFrameReceived(WebSocketFrame frame) 
	{
		logger.log(Level.INFO, "프레임 받음 >> " + frame);
	}
	
	@Override
	protected void debugFrameSent(WebSocketFrame frame) 
	{
		logger.log(Level.INFO, "프레임 보냄 >> " + frame);
	}
}