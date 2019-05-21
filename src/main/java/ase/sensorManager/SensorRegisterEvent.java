package ase.sensorManager;

import ase.sensorManager.sensor.Sensor;

public class SensorRegisterEvent
{
	public final Object source;
	public final boolean isActive;
	public final Sensor sensor;
	
	SensorRegisterEvent(Object source, boolean isActive, Sensor sensor)
	{
		this.source = source;
		this.isActive = isActive;
		this.sensor = sensor;
	}
}
