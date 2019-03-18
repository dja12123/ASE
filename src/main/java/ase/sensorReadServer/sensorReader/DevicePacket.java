package ase.sensorReadServer.sensorReader;

public class DevicePacket
{

	
	public final int ID;
	public final float X_GRADIANT;
	public final float Y_GRADIANT;
	public final float X_ACCEL;
	public final float Y_ACCEL;
	public final float Z_ACCEL;
	public final float Altitiude;
	public final float Temperature;
	
	public DevicePacket(int id, float xg, float yg, float xa, float ya, float za, float al, float tmp)
	{
		this.ID = id;
		this.X_GRADIANT = xg;
		this.Y_GRADIANT = yg;
		this.X_ACCEL = xa;
		this.Y_ACCEL = ya;
		this.Z_ACCEL = za;
		this.Altitiude = al;
		this.Temperature = tmp;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("ID: "); buffer.append(this.ID); buffer.append('\n');
		buffer.append("X_GRADIANT: "); buffer.append(this.X_GRADIANT); buffer.append('\n');
		buffer.append("Y_GRADIANT: "); buffer.append(this.Y_GRADIANT); buffer.append('\n');
		buffer.append("X_ACCEL: "); buffer.append(this.X_ACCEL); buffer.append('\n');
		buffer.append("Y_ACCEL: "); buffer.append(this.Y_ACCEL); buffer.append('\n');
		buffer.append("Z_ACCEL: "); buffer.append(this.Z_ACCEL); buffer.append('\n');
		buffer.append("Altitiude: "); buffer.append(this.Altitiude); buffer.append('\n');
		buffer.append("Temperature: "); buffer.append(this.Temperature); buffer.append('\n');
		
		return buffer.toString();
	}
}
