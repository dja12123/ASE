package ase.sensorComm.protocolSerial;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TransactionUnit
{
	public final long time;
	public final CommUser user;
	private final List<byte[]> receiveData;
	private short receiveCount;
	private short key;
	private short len;
	
	public TransactionUnit(long time, CommUser user)
	{
		this.time = time;
		this.user = user;
		this.receiveData = new ArrayList<>();
		
		this.key = -1;
		this.len = -1;
		this.receiveCount = 0;
	}
	
	public boolean putReceiveData(byte command, byte[] payload)
	{
		if(this.receiveData.size() == 0 && command == ProtoDef.SERIAL_PACKET_SEG_NODATACLIENT)
		{
			this.len = 0;
			return true;
		}
		else if(this.receiveData.size() == 0 && command == ProtoDef.SERIAL_PACKET_SEG_STARTFROMCLIENT)
		{
			ByteBuffer buffer = ByteBuffer.wrap(payload);
			this.key = buffer.getShort();
			this.len = buffer.getShort();
			byte[] temp = new byte[this.len - Short.BYTES - Short.BYTES];
			buffer.get(temp, Short.BYTES + Short.BYTES, temp.length);
			this.receiveData.add(temp);
		}
		else if(this.receiveData.size() > 0 && command == ProtoDef.SERIAL_PACKET_SEG_TRANSFROMCLIENT)
		{
			this.receiveData.add(payload);
		}
		else
		{
			return false;
		}
		this.receiveCount += payload.length;
		if(this.receiveCount > this.len)
		{
			return false;
		}
		return true;
	}
	
	public boolean isReceiveFinish()
	{
		if(this.receiveCount == this.len)
		{
			return true;
		}
		return false;
	}
	
	public short getkey()
	{
		return this.key;
	}
	
	public short getDataLen()
	{
		return this.len;
	}
	
	public byte[] getPayload()
	{
		ByteBuffer buffer = ByteBuffer.allocate(this.len - Short.BYTES - Short.BYTES);
		for(byte[] payload : this.receiveData)
		{
			buffer.put(payload);
		}
		return buffer.array();
	}
}
