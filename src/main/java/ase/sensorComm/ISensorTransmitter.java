package ase.sensorComm;

public interface ISensorTransmitter
{
	public boolean putSegment(short key, byte[] value);
	public boolean putSegment(short key, int data);
	public boolean putSegment(short key);
}
