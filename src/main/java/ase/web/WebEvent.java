package ase.web;

//import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class WebEvent 
{
	public final WebSocketChannel channel;
	public final String key;
	
	WebEvent(WebSocketChannel webSocketData, String key) 
	{
		this.key = key;
		this.channel = webSocketData;
	}
}