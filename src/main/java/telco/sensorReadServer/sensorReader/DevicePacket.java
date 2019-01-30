package telco.sensorReadServer.sensorReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import telco.sensorReadServer.appConnect.protocol.AppPacketDef;

public class DevicePacket
{
	public static final int FULL_PACKET_SIZE = 32;
	
	public final int ID;
	public final int NSIZE;
	public final float X_GRADIANT;
	public final float Y_GRADIANT;
	public final float X_ACCEL;
	public final float Y_ACCEL;
	public final float Z_ACCEL;
	public final float Altitiude;
	
	public static boolean isDevicePacket(byte[] packet)
	{
		if(packet.length != FULL_PACKET_SIZE)
		{
			return false;
		}
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		buffer.position(4);
		int packetSize = buffer.getInt();
		if(packetSize != FULL_PACKET_SIZE)
		{
			
			System.out.println(packetSize + " " + AppPacketDef.bytesToHex(packet, packet.length));
			return false;
		}
		return true;
	}
	
	public DevicePacket(byte[] packet)
	{
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		this.ID = buffer.getInt();
		this.NSIZE = buffer.getInt();
		this.X_GRADIANT = buffer.getFloat();
		this.Y_GRADIANT = buffer.getFloat();
		this.X_ACCEL = buffer.getFloat();
		this.Y_ACCEL = buffer.getFloat();
		this.Z_ACCEL = buffer.getFloat();
		this.Altitiude = buffer.getFloat();
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("ID: "); buffer.append(this.ID); buffer.append('\n');
		buffer.append("NSIZE: "); buffer.append(this.NSIZE); buffer.append('\n');
		buffer.append("X_GRADIANT: "); buffer.append(this.X_GRADIANT); buffer.append('\n');
		buffer.append("Y_GRADIANT: "); buffer.append(this.Y_GRADIANT); buffer.append('\n');
		buffer.append("X_ACCEL: "); buffer.append(this.X_ACCEL); buffer.append('\n');
		buffer.append("Y_ACCEL: "); buffer.append(this.Y_ACCEL); buffer.append('\n');
		buffer.append("Z_ACCEL: "); buffer.append(this.Z_ACCEL); buffer.append('\n');
		buffer.append("Altitiude: "); buffer.append(this.Altitiude); buffer.append('\n');
		
		return buffer.toString();
	}
}
