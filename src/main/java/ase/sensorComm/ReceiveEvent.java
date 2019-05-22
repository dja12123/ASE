package ase.sensorComm;

public class ReceiveEvent
{
	public final int ID;
	public final short key;
	public final byte[] payload;
	
	public ReceiveEvent(int id, short key, byte[] payload)
	{
		this.ID = id;
		this.key = key;
		this.payload = payload;
	}
}
