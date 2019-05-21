package ase.sensorManager.sensor;

public class Sensor
{	
	public final byte ID;
	
	public Sensor(byte id)
	{
		this.ID = id;
	}
	
	@Override
	public String toString()
	{
		return "sensor " + this.ID;
	}
}
