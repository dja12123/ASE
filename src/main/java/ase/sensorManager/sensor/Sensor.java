package ase.sensorManager.sensor;

public class Sensor
{	
	public final int ID;
	
	public Sensor(int id)
	{
		this.ID = id;
	}
	
	@Override
	public String toString()
	{
		return "sensor " + this.ID;
	}
}
