package ase.sensorComm.protocolSerial;

import java.util.ArrayList;
import java.util.List;

public class SerialReceiver
{
	public final long time;
	public final SerialTransmitter user;
	private final List<byte[]> receiveData;
	private boolean isEnd;
	
	public SerialReceiver(long time, SerialTransmitter user)
	{
		this.time = time;
		this.user = user;
		this.receiveData = new ArrayList<>();
		this.isEnd = true;
	}
	
	public boolean putReceiveData(byte[] packet)
	{
		if(packet.length < SerialProtoDef.SERIAL_PACKET_HEADERSIZE) return false;
		
		byte len = packet[0];
		byte id = packet[1];
		byte cmd = packet[2];
		
		if(id != this.user.ID || len != packet.length) return false;
		
		switch(cmd)
		{
		case SerialProtoDef.SERIAL_PACKET_SEG_NODATACLIENT:
			this.isEnd = true;
			break;
		
		case SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMCLIENT:
			this.receiveData.add(packet);
			break;
			
		case SerialProtoDef.SERIAL_PACKET_SEG_ENDFROMCLIENT:
			this.receiveData.add(packet);
			System.out.println("dataReceive");
			this.isEnd = true;
			break;
		}

		return true;
	}
	
	public List<byte[]> getReceiveData()
	{
		return this.receiveData;
	}
	
	public boolean isReceiveFinish()
	{
		return this.isEnd;
	}
}
