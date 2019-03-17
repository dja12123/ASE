package ase.web;

public class ChannelEvent
{
	public final WebSocketChannel channel;
	public final boolean isOpen;

	public ChannelEvent(WebSocketChannel channel, boolean isOpen)
	{
		this.channel = channel;
		this.isOpen = isOpen;
	}
}
