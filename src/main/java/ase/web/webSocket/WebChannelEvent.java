package ase.web.webSocket;

public class WebChannelEvent
{
	public final WebChannel channel;
	public final boolean isOpen;

	public WebChannelEvent(WebChannel channel, boolean isOpen)
	{
		this.channel = channel;
		this.isOpen = isOpen;
	}
}
