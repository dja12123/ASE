package telco.sensorReadServer.sensorManager.sensor;

public class DataReceiveEvent
{
	public final Sensor sensorInst;
	public final SensorData data;
	
	DataReceiveEvent(Sensor sensor, SensorData data)
	{
		this.sensorInst = sensor;
		this.data = data;
	}
	
	@Override
	public String toString()
	{
		return this.sensorInst.toString() + "\n" + this.data.toString();
	}
}
