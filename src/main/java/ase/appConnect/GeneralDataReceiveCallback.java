package ase.appConnect;

public interface GeneralDataReceiveCallback
{
	public void receiveData(Connection connection, String key, byte[][] data);
}
