package telco.sensorReadServer.sensorManager;

public class DataReceiveEvent
{
	public final Sensor sensorInst;
	
	public final float X_GRADIANT;
	public final float Y_GRADIANT;
	public final float X_ACCEL;
	public final float Y_ACCEL;
	public final float Z_ACCEL;
	public final float Altitiude;
	
	public DataReceiveEvent(Sensor sensor, int xg, int yg, int xa, int ya, int za, int al)
	{
		this.sensorInst = sensor;
		
		this.X_GRADIANT = xg;
		this.Y_GRADIANT = yg;
		this.X_ACCEL = xa;
		this.Y_ACCEL = ya;
		this.Z_ACCEL = za;
		this.Altitiude = al;
	}
}
