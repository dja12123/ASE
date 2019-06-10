package ase.sensorManager.sensorOnline;

import ase.sensorManager.sensor.Sensor;

public class SensorOnlineEvent
{
	public final Sensor sensor;
	public final boolean isOnline;
	
	public SensorOnlineEvent(Sensor sensor, boolean isOnline)
	{
		this.sensor = sensor;
		this.isOnline = isOnline;
	}
}
