package telco.appConnect.protocol;

public interface ChannelReceiveCallback
{
	public void receiveData(Channel ch, byte[][] data);
}
