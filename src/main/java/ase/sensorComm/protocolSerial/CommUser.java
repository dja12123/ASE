package ase.sensorComm.protocolSerial;

import java.util.ArrayList;
import java.util.List;

import ase.sensorComm.ISensorCommUser;
import ase.sensorComm.SegmentBuilder;

public class CommUser implements ISensorCommUser
{
	public final byte ID;

	private List<byte[]> packetList;
	private int totalPacketSize;

	public CommUser(byte id)
	{//주의! 데이터를 삽입할때 본 인스턴스에 동기화를 시행하시오.
		this.ID = id;
		this.packetList = new ArrayList<>();
		this.totalPacketSize = 0;
	}
	
	@Override
	public synchronized boolean putSegment(SegmentBuilder builder)
	{
		byte[] payload = builder.getPayload();
		int size = 0;
		List<byte[]> tempPacketList = new ArrayList<>();
		
		for(int i = 0; i < payload.length / SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE; ++i)
		{
			byte[] packet = new byte[SerialProtoDef.SERIAL_PACKET_MAXSIZE];

			packet[0] = this.ID;
			if(i == 0) packet[1] = SerialProtoDef.SERIAL_PACKET_SEG_STARTFROMSERVER;
			else packet[1] = SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMSERVER;
			packet[2] = SerialProtoDef.SERIAL_PACKET_MAXSIZE;
			System.arraycopy(payload, i * SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE, packet, SerialProtoDef.SERIAL_PACKET_HEADERSIZE, SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE);
			tempPacketList.add(packet);
			size += SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE;
		}
		
		if(payload.length % SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE > 0)
		{
			byte[] packet = new byte[payload.length % SerialProtoDef.SERIAL_PACKET_MAXSIZE + SerialProtoDef.SERIAL_PACKET_HEADERSIZE];
			packet[0] = this.ID;
			packet[1] = SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMSERVER;
			packet[2] = (byte)packet.length;
			System.arraycopy(payload, payload.length / SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE * SerialProtoDef.SERIAL_PACKET_MAXPAYLOADSIZE, packet, SerialProtoDef.SERIAL_PACKET_HEADERSIZE, payload.length % SerialProtoDef.SERIAL_PACKET_MAXSIZE);
			tempPacketList.add(packet);
			size += payload.length % SerialProtoDef.SERIAL_PACKET_MAXSIZE;
		}
		
		if(this.totalPacketSize + size >= SerialProtoDef.MAX_BUFFER_SIZE)
		{
			return false;
		}
		
		this.totalPacketSize += size;
		this.packetList.addAll(tempPacketList);
		return true;
	}
	
	public synchronized List<byte[]> popData()
	{
		List<byte[]> result = new ArrayList<>(this.packetList.size());
		result.addAll(this.packetList);
		this.packetList.clear();
		this.totalPacketSize = 0;
		return result;
	}
}
