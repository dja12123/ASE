package ase.sensorComm;

public interface ISensorTransmitter
{
	public int getID();
	public boolean isOnline();
	public boolean putSegment(short key, byte[] value);
	public boolean putSegment(short key, int data);
	public boolean putSegment(short key);
}
