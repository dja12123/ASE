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
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("sensor");
		buffer.append(this.sensor.id);
		buffer.append(" is ");
		if(this.isOnline) buffer.append("online");
		else buffer.append("offline");
		return buffer.toString();
	}
}
