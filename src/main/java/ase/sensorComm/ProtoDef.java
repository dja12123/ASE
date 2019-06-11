package ase.sensorComm;

public class ProtoDef
{
	public static final byte PACKET_MAXSIZE = 59;
	public static final byte SERIAL_PACKET_KEYSIZE = 2;
	public static final byte SERIAL_PACKET_BROADCAST_ADDR = 0x00;
	
	public static final short KEY_C2S_ACCELSENSOR_DATA = 0x0010;
	public static final short KEY_C2S_O2SENSOR_DATA = 0x0011;
	public static final short KEY_S2C_SAFETY_STAT = 0x0020;
	public static final short KEY_S2C_PLAY_SOUND = 0x0030;
	public static final short KEY_S2C_SW_CONTROL = 0x0031;
}
