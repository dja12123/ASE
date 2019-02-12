package telco.appConnect.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class AppDataPacketBuilder
{
	private final List<byte[]> payloadList;

	public AppDataPacketBuilder()
	{// 채널을 새로 생성할때 ID를 새로 부여.
		this.payloadList = new LinkedList<byte[]>();
	}
	
	public AppDataPacketBuilder appendData(String strPayload)
	{
		this.appendData(strPayload.getBytes());
		return this;
	}
	
	
	public AppDataPacketBuilder appendData(byte[] payload)
	{
		this.payloadList.add(payload);
		return this;
	}
	
	public byte writeOption(byte option)
	{
		if(this.payloadList.size() >= 1)
		{
			option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_PAYLOAD);
			if(this.payloadList.size() == 1)
			{
				option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_PAYLOAD_SINGLE);
			}
		}
		return option;
	}
	
	public byte[][] getPayload()
	{
		byte[][] payload = new byte[this.payloadList.size() + 1][];
		
		byte[] header;
		if(this.payloadList.size() == 1)
		{
			header = new byte[ProtocolDefine.RANGE_PAYLOAD_DATALEN];
			ByteBuffer buf = ByteBuffer.wrap(header);
			buf.putInt(this.payloadList.get(0).length);
		}
		else if(this.payloadList.size() > 1)
		{
			header = new byte[ProtocolDefine.RANGE_PAYLOAD_DATALEN
			                         + (this.payloadList.size() * ProtocolDefine.RANGE_PAYLOAD_DATALEN)];
			ByteBuffer buf = ByteBuffer.wrap(header);
			buf.putInt(this.payloadList.size());
			for(byte[] payloadSeg : this.payloadList)
			{
				buf.putInt(payloadSeg.length);
			}
		}
		else
		{
			header = new byte[ProtocolDefine.RANGE_PAYLOAD_DATALEN];
			payload = new byte[1][];
		}
		payload[0] = header;
		for(int i = 0; i < this.payloadList.size(); ++i)
		{
			payload[i + 1] = this.payloadList.get(i);
		}
		return payload;
	}
}
