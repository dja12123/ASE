package ase.sensorManager.sensorDataAccel;

import ase.sensorManager.sensor.Sensor;

public class AccelDataReceiveEvent
{
	public final Sensor sensorInst;
	public final SensorAccelData data;
	
	AccelDataReceiveEvent(Sensor sensor, SensorAccelData data)
	{
		this.sensorInst = sensor;
		this.data = data;
	}
	
	@Override
	public String toString()
	{
		return "dataReceive("+this.sensorInst.ID+ ")\n" + this.data.toString();
	}
}
