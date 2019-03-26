package ase.web.webSocket;

import ase.appConnect.channel.ProtocolDefine;
import ase.util.BinUtil;

public class ChannelDataEvent
{
	public final WebSocketChannel channel;
	public final byte[] data;
	
	public ChannelDataEvent(WebSocketChannel channel, byte[] data)
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
