package ase.web.webSocket;

import java.io.IOException;
import java.util.ArrayList;
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
	
	private List<WebChannel> channelList;
	public final Observable<WebChannelEvent> channelObservable;
	private Observer<WebChannelEvent> channelEventCallback;
	
	public WebSocketHandler(int port) 
	{
		super(port);
		logger.log(Level.INFO, "웹소켓 포트 " + port);
		this.channelList = new ArrayList<>();
		this.channelObservable = new Observable<>();
		this.channelEventCallback = this::channelEventObserver;
		this.addChannelObserver(this.channelEventCallback);
	}
	
	private void channelEventObserver(Observable<WebChannelEvent> provider, WebChannelEvent event)
	{
		if(!event.isOpen)
		{
			this.channelList.remove(event.channel);
		}
	}
	
	public void addChannelObserver(Observer<WebChannelEvent> observer)
	{
		this.channelObservable.addObserver(observer);
	}
	
	public void removeChannelObserver(Observer<WebChannelEvent> observer)
	{
		this.channelObservable.removeObserver(observer);
	}
	
	@Override
	protected synchronized WebSocket openWebSocket(IHTTPSession session) 
	{
		logger.log(Level.INFO, session.toString());
		WebChannel channel = new WebChannel(session, this.channelObservable);
		this.channelList.add(channel);
		return channel;
	}
	
	public synchronized void stopModule() 
	{
		this.channelObservable.removeObserver(this.channelEventCallback);
		
		for(WebChannel channel : this.channelList)
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
