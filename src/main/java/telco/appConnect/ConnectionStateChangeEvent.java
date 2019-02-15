package telco.appConnect;

public class ConnectionStateChangeEvent
{
	public final Connection connection;
	public final boolean isOpen;
	
	public ConnectionStateChangeEvent(Connection connection, boolean isOpen)
	{
		this.connection = connection;
		this.isOpen = isOpen;
	}
}
