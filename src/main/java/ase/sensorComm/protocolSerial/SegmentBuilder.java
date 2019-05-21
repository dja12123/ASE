package ase.sensorComm.protocolSerial;

import java.nio.ByteBuffer;

public class SegmentBuilder
{
	public final short key;
	public final short size;
	private final ByteBuffer buffer;
	
	public SegmentBuilder(short key, short size)
	{
		this.key = key;
		this.size = size;
		this.buffer = ByteBuffer.allocate(size + Short.BYTES + Short.BYTES);
		this.buffer.putShort(key);
		this.buffer.putShort(size);
	}
	
	public void put(byte value)
	{
		this.buffer.put(value);
	}
	
	public void putShort(short value)
	{
		this.buffer.putShort(value);
	}
	
	public void putChar(char value)
	{
		this.buffer.putChar(value);
	}
	
	public void putInt(int value)
	{
		this.buffer.putInt(value);
	}
	
	public void putLong(long value)
	{
		this.buffer.putLong(value);
	}
	
	public void putFloat(float value)
	{
		this.buffer.putFloat(value);
	}
	
	public void putDouble(double value)
	{
		this.buffer.putDouble(value);
	}
	
	public void putString(String value)
	{
		this.buffer.put(value.getBytes());
	}
	
	public byte[] getPayload()
	{
		return this.buffer.array();
	}
}
