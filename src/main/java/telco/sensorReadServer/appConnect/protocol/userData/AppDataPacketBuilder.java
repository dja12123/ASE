package telco.sensorReadServer.appConnect.protocol.userData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import telco.sensorReadServer.appConnect.protocol.ProtocolDefine;

public class AppDataPacketBuilder
{
	private final List<byte[]> payloadList;
	private int onlyPayloadSize;

	public AppDataPacketBuilder()
	{// 채널을 새로 생성할때 ID를 새로 부여.
		this.payloadList = new LinkedList<byte[]>();
		this.onlyPayloadSize = 0;
	}
	
	public AppDataPacketBuilder appendData(String strPayload) throws Exception
	{
		this.appendData(strPayload.getBytes());
		return this;
	}
	
	private static ArrayList<Integer> getControlMarkEscapePosition(byte[] payload)
	{
		int controlMarkCheck = 0;
		ArrayList<Integer> insertPosition = new ArrayList<Integer>();
		
		for(int i = 0; i < payload.length; ++i)
		{
			if(payload[i] == ProtocolDefine.CONTROL_MARK[controlMarkCheck])
			{
				++controlMarkCheck;
				if(controlMarkCheck >= ProtocolDefine.CONTROL_MARK.length)
				{
					insertPosition.add(i - (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length));
					controlMarkCheck = 0;
				}
			}
			else
			{
				if(payload[i] == ProtocolDefine.CONTROL_DATA_ESCAPE[0]) controlMarkCheck = 1;
				else controlMarkCheck = 0;
			}
		}
		return insertPosition;
	}
	
	public AppDataPacketBuilder appendData(byte[] payload) throws Exception
	{
		ArrayList<Integer> insertPosition = getControlMarkEscapePosition(payload);
		
		int addPayloadSize = this.onlyPayloadSize + payload.length
				+ (insertPosition.size() * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
				+ ProtocolDefine.CONTROL_SEQ_SIZE;
		int segmentCount = addPayloadSize / ProtocolDefine.RANGE_PAYLOAD;
		if(addPayloadSize % ProtocolDefine.RANGE_PAYLOAD != 0) segmentCount += 1;
		
		if(addPayloadSize + (segmentCount * ProtocolDefine.PACKET_METADATA_SIZE) > ProtocolDefine.FULL_PACKET_LIMIT)
		{
			throw new Exception("payload size over");
		}
		if(insertPosition.size() > 0)
		{
			byte[] convertPayload = new byte[payload.length + (insertPosition.size() * 
					(ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length)) + ProtocolDefine.CONTROL_SEQ_SIZE];
			int beforeMark = 0;
			for(int i = 0; i < insertPosition.size(); ++i)
			{
				System.arraycopy(payload
						, beforeMark
						, convertPayload
						, beforeMark + (i * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
						, insertPosition.get(i) - beforeMark);
				beforeMark = insertPosition.get(i);
			}
			System.arraycopy(payload
					, beforeMark
					, convertPayload
					, insertPosition.get(insertPosition.size() - 1) + (insertPosition.size()
							* (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
					, payload.length - beforeMark);
			for(int i = 0; i < insertPosition.size(); ++i)
			{
				System.arraycopy(ProtocolDefine.CONTROL_DATA_ESCAPE
						, 0
						, convertPayload
						, insertPosition.get(i) + (i * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
						, ProtocolDefine.CONTROL_DATA_ESCAPE.length);
			}
			payload = convertPayload;
		}
		else
		{
			byte[] convertPayload = new byte[payload.length + ProtocolDefine.CONTROL_SEQ_SIZE];
			System.arraycopy(payload
					, 0
					, convertPayload
					, 0
					, payload.length);
			payload = convertPayload;
		}
		System.arraycopy(ProtocolDefine.CONTROL_DATA_SEPARATOR
				, 0
				, payload
				, payload.length - ProtocolDefine.CONTROL_SEQ_SIZE
				, ProtocolDefine.CONTROL_SEQ_SIZE);

		this.onlyPayloadSize += payload.length;
		this.payloadList.add(payload);
		return this;
	}
	
	public byte[] createData()
	{
		int size = 0;
		int pos = 0;
		byte[] data;
		for(int i = 0; i < this.payloadList.size(); ++i)
		{
			size += this.payloadList.get(i).length;
		}
		data = new byte[size];
		for(byte[] payload : this.payloadList)
		{
			System.arraycopy(payload,0, data, pos, payload.length);
			pos += payload.length;
		}
		return data;
	}
}
