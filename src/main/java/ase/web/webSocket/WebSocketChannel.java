package ase.web.webSocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import ase.util.observer.Observable;
import ase.util.observer.Observer;

/*
 * WebSocket의 데이터를 정의한 클래스
 * @extends WebSocket
 */
public class WebSocketChannel extends WebSocket
{
	public static final Logger logger = WebSocketHandler.logger;
	private Observable<ChannelEvent> openCloseWSProvider;
	private Observable<ChannelDataEvent> dataReceiveProvider;
	private String key;
	
	public WebSocketChannel(IHTTPSession session, Observable<ChannelEvent> channelObservable)
	{
		super(session);
		this.dataReceiveProvider = new Observable<>();
		this.openCloseWSProvider = channelObservable;
		this.key = null;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public void addDataReceiveObserver(Observer<ChannelDataEvent> observer)
	{
		this.dataReceiveProvider.addObserver(observer);
	}
	
	public void removeDataReceiveObserver(Observer<ChannelDataEvent> observer)
	{
		this.dataReceiveProvider.removeObserver(observer);
	}
	
	@Override
	protected void onOpen() 
	{
		logger.log(Level.INFO, "웹소켓 열림");
	}
	
	@Override
	protected synchronized void onClose(CloseCode code, String reason, boolean initiatedByRemote) 
	{
		if(this.key != null)
		{
			this.dataReceiveProvider.clearObservers();
			ChannelEvent channelEvent = new ChannelEvent(this, false);
			this.openCloseWSProvider.notifyObservers(channelEvent);
		}
		
		String logMsg = "웹소켓 닫힘 [" + (initiatedByRemote ? "Remote" : "Self") + "]"
						+ (code != null ? code : "UnknownCloseCode[" + code + "]")
						+ (reason != null && !reason.isEmpty() ? ": " + reason : "");
		logger.log(Level.INFO, logMsg);
	}
	
	@Override
	protected synchronized void onMessage(WebSocketFrame frame) 
	{
		if(this.key == null)
		{
			String key = frame.getTextPayload();
			this.key = key;
			ChannelEvent channelEvent = new ChannelEvent(this, true);
			this.openCloseWSProvider.notifyObservers(channelEvent);
			return;
		}
		
		ChannelDataEvent channelDataEvent = new ChannelDataEvent(this, frame.getBinaryPayload());
		this.dataReceiveProvider.notifyObservers(channelDataEvent);
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
	
	@Override
	public String toString()
	{
		return "WS Channel key:"+this.key+" isOpen:"+this.isOpen()+" hashcode:"+this.hashCode();
	}
	
	public void normalClose()
	{
		try
		{
			this.close(CloseCode.NormalClosure, "normal Close", false);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 종료중 오류", e);
		}
	}
}