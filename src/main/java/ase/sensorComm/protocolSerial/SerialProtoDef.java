package ase.sensorComm.protocolSerial;

import ase.sensorComm.ProtoDef;

public class SerialProtoDef
{
	public static final int SEND_BUFFER_SIZE = 400;
	
	public static final int SERIAL_DELAY = 70;
	public static final int SERIAL_TRANSACTION_TIMEOUT = 500;
	
	public static final byte SERIAL_PACKET_HEADERSIZE = 3;
	public static final byte SERIAL_PACKET_MAXVALSIZE = ProtoDef.PACKET_MAXSIZE - SERIAL_PACKET_HEADERSIZE - ProtoDef.SERIAL_PACKET_KEYSIZE;

	public static final byte SERIAL_PACKET_SEG_TRANSFROMSERVER = 0x01;
	public static final byte SERIAL_PACKET_SEG_ENDFROMSERVER = 0x02;
	public static final byte SERIAL_PACKET_SEG_NODATASERVER = 0x03;

	public static final byte SERIAL_PACKET_SEG_NODATACLIENT = 0x11;
	public static final byte SERIAL_PACKET_SEG_TRANSFROMCLIENT = 0x12;
	public static final byte SERIAL_PACKET_SEG_ENDFROMCLIENT = 0x13;
}
