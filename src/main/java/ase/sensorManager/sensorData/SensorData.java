package ase.sensorManager.sensorData;

import java.util.Date;

public class SensorData
{
	public final Date time;
	public final float X_GRADIANT;
	public final float Y_GRADIANT;
	public final float X_ACCEL;
	public final float Y_ACCEL;
	public final float Z_ACCEL;
	public final float Altitiude;
	
	public SensorData(Date time, float xg, float yg, float xa, float ya, float za, float al)
	{
		this.time = time;
		this.X_GRADIANT = xg;
		this.Y_GRADIANT = yg;
		this.X_ACCEL = xa;
		this.Y_ACCEL = ya;
		this.Z_ACCEL = za;
		this.Altitiude = al;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("TIME: "); buffer.append(this.time.toString()); buffer.append('\n');
		buffer.append("X_GRADIANT: "); buffer.append(this.X_GRADIANT); buffer.append('\n');
		buffer.append("Y_GRADIANT: "); buffer.append(this.Y_GRADIANT); buffer.append('\n');
		buffer.append("X_ACCEL: "); buffer.append(this.X_ACCEL); buffer.append('\n');
		buffer.append("Y_ACCEL: "); buffer.append(this.Y_ACCEL); buffer.append('\n');
		buffer.append("Z_ACCEL: "); buffer.append(this.Z_ACCEL); buffer.append('\n');
		buffer.append("Altitiude: "); buffer.append(this.Altitiude);
		
		return buffer.toString();
	}
}
