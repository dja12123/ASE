package ase.clientSession;

public class ChannelEvent
{
	public final IChannel channel;
	public final boolean isOpen;

	public ChannelEvent(IChannel channel, boolean isOpen)
	{
		this.channel = channel;
		this.isOpen = isOpen;
	}
}
