package ase.web;

import ase.appConnect.channel.ProtocolDefine;

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
		return "Channel Data count:"+this.data.length+" raw:"+ProtocolDefine.bytesToHex(data, data.length);
	}
}
