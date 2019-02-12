package telco.sensorReadServer.appConnect;

import telco.sensorReadServer.appConnect.protocol.Channel;
import telco.sensorReadServer.appConnect.protocol.Connection;

public class AppDataReceiveEvent
{	
	public final boolean hasChannel;
	
	public final Connection connection;
	public final String key;
	
	public final Channel channel;
	public final byte[][] payload;
	
	
	AppDataReceiveEvent(Connection connection, Channel channel)
	{
		this.hasChannel = true;
		this.connection = connection;
		this.key = channel.key;
		this.channel = channel;
		this.payload = null;
	}
	
	AppDataReceiveEvent(Connection connection, String key, byte[][] payload)
	{
		this.hasChannel = false;
		this.connection = connection;
		this.key = key;
		this.channel = null;
		this.payload = payload;
	}
}
