package ase.sensorManager.accelSensorDataAnalyser;

import ase.sensorManager.sensor.Sensor;

public class SafeStateChangeEvent
{
	public final Sensor sensor;
	public final SafetyStatus status;
	
	public SafeStateChangeEvent(Sensor sensor, SafetyStatus status)
	{
		this.sensor = sensor;
		this.status = status;
	}
}
