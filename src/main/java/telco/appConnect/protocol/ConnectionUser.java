package telco.appConnect.protocol;

public interface ConnectionUser
{
	public void startConnection(Connection connection);
	public void closeConnection(Connection connection);
	public void createChannel(Connection connection, Channel channel);
	public void receiveGeneralData(Connection connection, String key, byte[][] data);
}
