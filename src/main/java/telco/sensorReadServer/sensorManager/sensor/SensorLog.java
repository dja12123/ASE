package telco.sensorReadServer.sensorManager.sensor;

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
	
}
