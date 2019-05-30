package ase.sensorManager.sensorData;

import java.util.Date;

public class SensorData
{
	public final Date time;
	public final int X_ACCEL;
	public final int Y_ACCEL;
	public final int Z_ACCEL;
	
	public SensorData(Date time, int xa, int ya, int za)
	{
		this.time = time;
		this.X_ACCEL = xa;
		this.Y_ACCEL = ya;
		this.Z_ACCEL = za;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("TIME: "); buffer.append(this.time.toString()); buffer.append('\n');
		buffer.append("X_ACCEL: "); buffer.append(this.X_ACCEL); buffer.append('\n');
		buffer.append("Y_ACCEL: "); buffer.append(this.Y_ACCEL); buffer.append('\n');
		buffer.append("Z_ACCEL: "); buffer.append(this.Z_ACCEL); buffer.append('\n');
		
		return buffer.toString();
	}
}
