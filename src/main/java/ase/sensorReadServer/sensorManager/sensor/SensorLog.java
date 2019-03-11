package ase.sensorReadServer.sensorManager.sensor;

import java.util.Date;
import java.util.logging.Level;

public class SensorLog
{
	public final Level level;
	public final Date time;
	public final String message;
	
	public SensorLog(Level level, Date time, String message)
	{
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
