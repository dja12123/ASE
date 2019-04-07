package ase.sensorManager;

import ase.sensorManager.sensor.Sensor;

public class SensorRegisterEvent
{
	public final boolean isActive;
	public final Sensor sensor;
	
	SensorRegisterEvent(boolean isActive, Sensor sensor)
	{
		this.isActive = isActive;
		this.sensor = sensor;
	}
}
