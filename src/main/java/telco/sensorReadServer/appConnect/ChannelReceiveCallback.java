package telco.sensorReadServer.appConnect;

public interface ChannelReceiveCallback
{
	public void receiveData(Channel ch, byte[][] data);
}
