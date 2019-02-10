package telco.sensorReadServer.appConnect;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class AppDataPacketBuilder
{
	private final List<byte[]> payloadList;
	private final short channel;

	AppDataPacketBuilder(short channel)
	{// 채널을 새로 생성할때 ID를 새로 부여.
		this.payloadList = new LinkedList<byte[]>();
		this.channel = channel;
	}
	
	public AppDataPacketBuilder appendData(String strPayload) throws Exception
	{
		this.appendData(strPayload.getBytes());
		return this;
	}
	
	
	public AppDataPacketBuilder appendData(byte[] payload) throws Exception
	{
		this.payloadList.add(payload);
		return this;
	}
	
	public byte[] getMetadata()
	{
		byte option = ProtocolDefine.OPTION_CHANNEL;
		option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_CHANNEL_PAYLOAD);
		
		if(this.payloadList.size() == 1)
		{
			byte[] header = new byte[ProtocolDefine.RANGE_CHANNEL_PAYLOAD_HEADER];
			ByteBuffer buf = ByteBuffer.wrap(header);
			option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_CHANNEL_PAYLOAD_SINGLE);
			buf.put(option);
			buf.putShort(this.channel);
			buf.putInt(this.payloadList.get(0).length);
			return header;
		}
		else if(this.payloadList.size() > 1)
		{
			byte[] header = new byte[ProtocolDefine.RANGE_CHANNEL_PAYLOAD_HEADER
			                         + (this.payloadList.size() * ProtocolDefine.RANGE_CHANNEL_PAYLOAD_DATALEN)];
			ByteBuffer buf = ByteBuffer.wrap(header);
			buf.put(option);
			buf.putShort(this.channel);
			buf.putInt(this.payloadList.size());
			for(byte[] payload : this.payloadList)
			{
				buf.putInt(payload.length);
			}
			return header;
		}
		throw new RuntimeException("빈 패킷을 생성할수 없음");
	}
	
	public byte[][] getPayload()
	{
		byte[][] payload = new byte[this.payloadList.size()][];
		this.payloadList.toArray(payload);
		return payload;
	}
}
