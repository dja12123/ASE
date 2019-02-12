package telco.appConnect;

import telco.appConnect.protocol.Connection;

public class ConnectionStateChangeEvent
{
	public final Connection connection;
	public final boolean isOpen;
	
	ConnectionStateChangeEvent(Connection connection, boolean isOpen)
	{
		this.connection = connection;
		this.isOpen = isOpen;
	}
}
