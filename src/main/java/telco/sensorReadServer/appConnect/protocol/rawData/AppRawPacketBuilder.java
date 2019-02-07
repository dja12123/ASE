package telco.sensorReadServer.appConnect.protocol.rawData;

import java.nio.ByteBuffer;
import java.util.Date;

import telco.sensorReadServer.appConnect.protocol.ProtocolDefine;

public class AppRawPacketBuilder
{
	public final short channelID;
	private final byte[][] rawPacket;
	private int nowPosition;
	private int onlyPayloadSize;
	
	private Date updateTime;

	public AppRawPacketBuilder(byte[] firstPacket)
	{
		ByteBuffer buffer = ByteBuffer.wrap(firstPacket);
		buffer.position(ProtocolDefine.START_PACKET_CHANNEL);
		this.channelID = buffer.getShort();
		this.rawPacket = new byte[buffer.getShort()][];
		this.rawPacket[0] = firstPacket;
		this.nowPosition = 1;
		
		this.updateTime = new Date();
	}
	
	public boolean appendData(byte[] rawPacket)
	{// 완성시 true반환
		this.updateTime = new Date();
		this.rawPacket[this.nowPosition] = rawPacket;
		this.nowPosition++;
		if(this.nowPosition >= this.rawPacket.length)
		{
			return true;
		}
		return false;
	}
	
	public boolean isTimeout(Date date)
	{
		return false;
	}
	
	public byte[] getPayload()
	{
		int payloadSize = this.rawPacket.length * ProtocolDefine.RANGE_PAYLOAD;
		if(this.rawPacket[this.rawPacket.length - 1].length < ProtocolDefine.MAX_SEGMENT_SIZE)
			payloadSize -= ProtocolDefine.MAX_SEGMENT_SIZE - this.rawPacket[this.rawPacket.length - 1].length;
		
		byte[] payload = new byte[payloadSize];
		for(int i = 0; i < this.rawPacket.length; ++i)
		{
			System.arraycopy(this.rawPacket[i]
					, ProtocolDefine.START_PAYLOAD
					, payload, i * ProtocolDefine.RANGE_PAYLOAD
					, this.rawPacket[i].length - ProtocolDefine.PACKET_METADATA_SIZE);
		}
		
		return payload;
	}
	
	public short getChannelID()
	{
		return this.channelID;
	}
}
