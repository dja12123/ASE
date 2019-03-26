package ase.web.webSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;

import ase.console.LogWriter;
import ase.util.observer.Observable;
import ase.util.observer.Observer;


public class WebSocketHandler extends NanoWSD
{
	public static final String KEY_DATA_SEPERATOR = "=";
	
	public static final Logger logger = LogWriter.createLogger(WebSocketHandler.class, "websocket");
	
	private List<WebSocketChannel> channelList;
	public final Observable<ChannelEvent> channelObservable;
	private Observer<ChannelEvent> channelEventCallback;
	private HashMap<IHTTPSession, WebSocketChannel> sessionMap;
	
	public WebSocketHandler(int port) 
	{
		super(port);
		logger.log(Level.INFO, "웹소켓 열기 " + port);
		this.channelList = new ArrayList<>();
		this.channelObservable = new Observable<>();
		this.sessionMap = new HashMap<>();
		this.channelEventCallback = this::channelEventCallback;
		this.addChannelObserver(this.channelEventCallback);
	}
	
	private void channelEventCallback(Observable<ChannelEvent> provider, ChannelEvent event)
	{
		if(!event.isOpen)
		{
			this.channelList.remove(event.channel);
		}
	}
	
	public void addChannelObserver(Observer<ChannelEvent> observer)
	{
		this.channelObservable.addObserver(observer);
	}
	
	public void removeChannelObserver(Observer<ChannelEvent> observer)
	{
		this.channelObservable.removeObserver(observer);
	}
	
	@Override
	protected synchronized WebSocket openWebSocket(IHTTPSession session) 
	{
		logger.log(Level.INFO, session.toString());
		WebSocketChannel channel = new WebSocketChannel(session, this.channelObservable);
		this.channelList.add(channel);
		return channel;
	}
	
	public synchronized void stopModule() 
	{
		this.channelObservable.removeObserver(this.channelEventCallback);
		
		for(WebSocketChannel channel : this.channelList)
		{
			try
			{
				channel.close(CloseCode.NormalClosure, "normal close", false);
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, "웹소켓 종료중 오류");
			}
		}
		logger.log(Level.INFO, "웹소켓 종료");
		
		this.channelObservable.clearObservers();
	}
}
