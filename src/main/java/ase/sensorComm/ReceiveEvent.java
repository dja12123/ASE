package ase.sensorComm;

public class ReceiveEvent
{
	public final int userID;
	public final short key;
	public final byte[] payload;
	
	public ReceiveEvent(int userID, short key, byte[] payload)
	{
		this.userID = userID;
		this.key = key;
		this.payload = payload;
	}
}
