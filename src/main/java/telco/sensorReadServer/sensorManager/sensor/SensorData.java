package telco.sensorReadServer.sensorManager.sensor;

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
}
