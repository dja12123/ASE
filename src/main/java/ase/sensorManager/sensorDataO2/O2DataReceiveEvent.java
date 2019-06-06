package ase.sensorManager.sensorDataO2;

import ase.sensorManager.sensor.Sensor;

public class O2DataReceiveEvent
{
	public final Sensor sensorInst;
	public final SensorO2Data data;
	
	O2DataReceiveEvent(Sensor sensor, SensorO2Data data)
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
