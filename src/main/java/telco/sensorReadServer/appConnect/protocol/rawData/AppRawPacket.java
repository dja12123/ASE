package telco.sensorReadServer.appConnect.protocol.rawData;

import java.nio.ByteBuffer;

import telco.sensorReadServer.appConnect.protocol.ProtocolDefine;

public class AppRawPacket
{
	private final byte[] payload;
	public final int segmentCount;
	public final short channel;
	
	public AppRawPacket(short channel, byte[] payload)
	{
		int segmentCount = payload.length / ProtocolDefine.RANGE_PAYLOAD;
		if(payload.length % ProtocolDefine.RANGE_PAYLOAD != 0) ++segmentCount;
		
		this.payload = payload;
		this.segmentCount = segmentCount;
		this.channel = channel;
	}
	
	public byte[][] getRawData()
	{
		byte[][] rawData = new byte[segmentCount][];
		
		int remainingCapacity = payload.length;
		
		for(short i = 0; i < segmentCount; ++i)
		{
			byte[] taskArray;
			if(remainingCapacity >= ProtocolDefine.RANGE_PAYLOAD)
			{
				taskArray = new byte[ProtocolDefine.MAX_SEGMENT_SIZE];
			}
			else
			{
				taskArray = new byte[remainingCapacity + ProtocolDefine.PACKET_METADATA_SIZE];
				
				
			}
			System.arraycopy(this.payload
					, i * ProtocolDefine.RANGE_PAYLOAD
					, taskArray
					, ProtocolDefine.START_PAYLOAD
					, taskArray.length - ProtocolDefine.PACKET_METADATA_SIZE);
			ByteBuffer buf = ByteBuffer.wrap(taskArray);
			buf.put(ProtocolDefine.CONTROL_START);
			buf.putShort((this.channel));
			buf.putShort((short)segmentCount);
			buf.putShort(i);
			buf.position(taskArray.length - ProtocolDefine.CONTROL_END.length);
			buf.put(ProtocolDefine.CONTROL_END);
			remainingCapacity -= ProtocolDefine.RANGE_PAYLOAD;
			
			rawData[i] = taskArray;
		}
		
		return rawData;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("channel: "); buf.append(this.channel); buf.append('\n');
		buf.append("segmentCount: "); buf.append(this.segmentCount); buf.append('\n');
		buf.append(ProtocolDefine.bytesToHex(this.payload, this.payload.length));
		return buf.toString();
	}
}
