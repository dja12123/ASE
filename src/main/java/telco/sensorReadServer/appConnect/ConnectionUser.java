package telco.sensorReadServer.appConnect;

public interface ConnectionUser
{
	public void createChannel(Connection connection, Channel channel);
	public void closeConnection(Connection connection);
}
