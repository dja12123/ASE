package ase.sensorManager.sensorLog;

import java.util.Date;
import java.util.logging.Level;

import ase.sensorManager.sensor.Sensor;

public class SensorLog
{
	public final Sensor sensor;
	public final Level level;
	public final Date time;
	public final String message;
	
	public SensorLog(Sensor sensor, Level level, Date time, String message)
	{
		this.sensor = sensor;
		this.level = level;
		this.time = time;
		this.message = message;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("["); buffer.append(this.time.toString()); buffer.append("][");
		buffer.append(this.level.toString()); buffer.append("]");
		buffer.append(this.message);
		return buffer.toString();
	}
}
