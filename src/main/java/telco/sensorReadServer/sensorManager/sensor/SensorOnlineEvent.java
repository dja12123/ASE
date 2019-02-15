package telco.sensorReadServer.sensorManager.sensor;

public class SensorOnlineEvent
{
	public final Sensor sensor;
	public final boolean isOnline;
	
	SensorOnlineEvent(Sensor sensor, boolean isOnline)
	{
		this.sensor = sensor;
		this.isOnline = isOnline;
	}
}
