package ase.sensorReadServer.sensorManager;

import ase.sensorReadServer.sensorManager.sensor.Sensor;

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
