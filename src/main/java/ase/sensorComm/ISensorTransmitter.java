package ase.sensorComm;

public interface ISensorTransmitter
{
	public boolean putSegment(short key, byte[] value);
}
