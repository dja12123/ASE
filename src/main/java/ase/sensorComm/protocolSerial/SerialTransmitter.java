package ase.sensorComm.protocolSerial;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ase.sensorComm.ISensorTransmitter;
import ase.sensorComm.ProtoDef;

public class SerialTransmitter implements ISensorTransmitter
{
	public final byte ID;
	private List<byte[]> packetList;
	
	public SerialTransmitter(byte id)
	{
		this.ID = id;
		this.packetList = new ArrayList<>();
	}
	
	@Override
	public synchronized boolean putSegment(short key, byte[] value)
	{
		if(value.length > SerialProtoDef.SERIAL_PACKET_MAXVALSIZE) return false;
		byte sendSize = (byte) (SerialProtoDef.SERIAL_PACKET_HEADERSIZE + ProtoDef.SERIAL_PACKET_KEYSIZE + value.length);
		ByteBuffer buf = ByteBuffer.allocate(sendSize);
		buf.put(sendSize);
		buf.put(this.ID);
		buf.put(SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMSERVER);
		buf.putShort(key);
		buf.put(value);
		this.packetList.add(buf.array());
		return true;
	}
	
	@Override
	public boolean putSegment(short key, int value)
	{
		byte sendSize = (byte) (SerialProtoDef.SERIAL_PACKET_HEADERSIZE + ProtoDef.SERIAL_PACKET_KEYSIZE + 4);
		ByteBuffer buf = ByteBuffer.allocate(sendSize);
		buf.put(sendSize);
		buf.put(this.ID);
		buf.put(SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMSERVER);
		buf.putShort(key);
		buf.putInt(value);
		this.packetList.add(buf.array());
		return true;
	}
	
	@Override
	public boolean putSegment(short key)
	{
		byte sendSize = (byte) (SerialProtoDef.SERIAL_PACKET_HEADERSIZE + ProtoDef.SERIAL_PACKET_KEYSIZE);
		ByteBuffer buf = ByteBuffer.allocate(sendSize);
		buf.put(sendSize);
		buf.put(this.ID);
		buf.put(SerialProtoDef.SERIAL_PACKET_SEG_TRANSFROMSERVER);
		buf.putShort(key);
		this.packetList.add(buf.array());
		return true;
	}
	
	public synchronized List<byte[]> popData()
	{
		List<byte[]> result = new ArrayList<>(this.packetList.size());
		result.addAll(this.packetList);
		this.packetList.clear();
		return result;
	}


}
