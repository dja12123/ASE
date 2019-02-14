package telco.sensorReadServer.sensorManager;

public class SensorStateChangeEvent
{
	public final Sensor sensor;
	public final boolean isOnline;
	
	public SensorStateChangeEvent(Sensor sensor, boolean isOnline)
	{
		this.sensor = sensor;
		this.isOnline = isOnline;
	}
}
