package ase.sensorComm.protocolSerial;

public class ProtoDef
{
	public static final int MAX_BUFFER_SIZE = 400;
	public static final int SERIAL_PACKET_MAXSIZE = 59;
	public static final int SERIAL_PACKET_HEADERSIZE = 3;
	public static final int SERIAL_PACKET_MAXPAYLOADSIZE = SERIAL_PACKET_MAXSIZE - SERIAL_PACKET_HEADERSIZE;
	public static final int SERIAL_PACKET_SEG_STARTFROMSERVER = 0x01;
	public static final int SERIAL_PACKET_SEG_TRANSFROMSERVER = 0x02;
	public static final int SERIAL_PACKET_SEG_NODATASERVER = 0x03;
	public static final int SERIAL_PACKET_SEG_STARTFROMCLIENT = 0x11;
	public static final int SERIAL_PACKET_SEG_TRANSFROMCLIENT = 0x12;
	public static final int SERIAL_PACKET_SEG_NODATACLIENT = 0x13;
}
