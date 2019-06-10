package ase.sensorComm;

public class CommOnlineEvent
{
	public final int ID;
	public final boolean isOnline;
	
	public CommOnlineEvent(int id, boolean isOnline)
	{
		this.ID = id;
		this.isOnline = isOnline;
	}
}
