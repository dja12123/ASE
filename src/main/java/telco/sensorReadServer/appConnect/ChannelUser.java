package telco.sensorReadServer.appConnect;

public interface ChannelUser
{
	public void receiveData(Channel ch, byte[][] data);
	public void closeChannel(Channel ch);
}
