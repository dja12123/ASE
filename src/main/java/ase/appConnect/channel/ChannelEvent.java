package ase.appConnect.channel;

public class ChannelEvent
{
	public final Channel channel;
	public final boolean isOpen;

	public ChannelEvent(Channel channel, boolean isOpen)
	{
		this.channel = channel;
		this.isOpen = isOpen;
	}
}
