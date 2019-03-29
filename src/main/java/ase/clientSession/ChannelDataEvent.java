package ase.clientSession;

import ase.util.BinUtil;
import ase.web.webSocket.WebChannel;

public class ChannelDataEvent
{
	public final WebChannel channel;
	public final byte[] data;
	
	public ChannelDataEvent(WebChannel channel, byte[] data)
	{
		this.channel = channel;
		this.data = data;
	}
	
	@Override
	public String toString()
	{
		return "Channel Data count:"+this.data.length+" raw:"+BinUtil.bytesToHex(data);
	}
}
