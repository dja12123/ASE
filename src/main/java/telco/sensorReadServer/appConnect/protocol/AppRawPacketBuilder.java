package telco.sensorReadServer.appConnect.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class AppRawPacketBuilder
{
	public static void main(String[] args) throws Exception
	{
		AppRawPacketBuilder builder = new AppRawPacketBuilder((short) 123);
		builder.appendData(new byte[]{0x11, 0x11, 0x11,0x11, 0x11, 0x10,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11});
		builder.appendData(new byte[]{0x11});
		builder.appendData(new byte[]{0x22});
		builder.appendData(new byte[]{0x11, 0x11, 0x11,0x11, 0x11, 0x10,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11});
		builder.appendData(new byte[]{0x11, 0x11, 0x11,0x11, 0x11, 0x10,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11});
		System.out.println(builder.createPacketInst().toString());
	}
	private final short channelID;
	private final List<byte[]> payloadList;
	private short option;
	private int onlyPayloadSize;

	public AppRawPacketBuilder(short channelID)
	{// 채널을 새로 생성할때 ID를 새로 부여.
		this.channelID = channelID;
		this.payloadList = new LinkedList<byte[]>();
		this.option = AppPacketDef.OPT_KEY_PACKET;
		this.onlyPayloadSize = 0;
	}
	
	public AppRawPacketBuilder(AppPacket appPacket)
	{// 기존 채널을 사용할때.
		this.channelID = appPacket.getChannelID();
		this.payloadList = new LinkedList<byte[]>();
		this.option = AppPacketDef.OPT_DATA_PACKET;
		this.onlyPayloadSize = 0;
	}
	
	public AppRawPacketBuilder appendData(String strPayload) throws Exception
	{
		this.appendData(strPayload.getBytes());
		return this;
	}
	
	public AppRawPacketBuilder appendData(byte[] payload) throws Exception
	{
		int convertCount = 0;
		
		for(int i = 0; i < payload.length; ++i)
		{
			if(payload[i] == AppPacketDef.CONTROL_MARK)
			{
				++convertCount;
			}
		}
		
		int addPayloadSize = this.onlyPayloadSize + payload.length
				+ convertCount + AppPacketDef.CONTROL_SEQ_SIZE;
		int segmentCount = addPayloadSize / AppPacketDef.RANGE_PAYLOAD;
		if(addPayloadSize % AppPacketDef.RANGE_PAYLOAD != 0) segmentCount += 1;
		
		if(addPayloadSize + (segmentCount * AppPacketDef.PACKET_METADATA_SIZE) > AppPacketDef.FULL_PACKET_LIMIT)
		{
			throw new Exception("payload size over");
		}
		
		byte[] convertPayload = new byte[payload.length + convertCount + AppPacketDef.CONTROL_SEQ_SIZE];
		int nowChange = 0;
		for(int i = 0; i < payload.length; ++i)
		{
			convertPayload[i + nowChange] = payload[i];
			if(payload[i] == AppPacketDef.CONTROL_MARK)
			{
				++nowChange;
				convertPayload[i + nowChange] = AppPacketDef.CONTROL_DATA_TO_ZERO[1];
			}
		}
		convertPayload[convertPayload.length - 2] = AppPacketDef.CONTROL_DATA_SEPARATOR[0];
		convertPayload[convertPayload.length - 1] = AppPacketDef.CONTROL_DATA_SEPARATOR[1];
		payload = convertPayload;
	
	
		this.onlyPayloadSize += payload.length;
		
		this.payloadList.add(payload);
		return this;
	}
	
	public AppPacket createPacketInst()
	{
		int taskSegNo = -1;
		int taskPayloadNo = 0;
		int nowSegmentCapacity = 0;
		int nowPayloadPointer = 0;
		int remainingCapacity = this.onlyPayloadSize;
		
		System.out.println(remainingCapacity);

		List<byte[]> assembledPacket = new LinkedList<byte[]>();
		
		byte[] nowTaskArray = null;
		byte[] nowTaskPayload = null;
		
		while(true)
		{// 재설계 필요 패이로드에 더 긴 이스케이프 시퀀스(4자리) 를 삽입하도록 변경 할 경우 길이조절에 복잡한 로직이 필요 없음 0x-7FFF0000을 이스케이프 시퀀스로 활용하면 잘려도 문제없음...
			nowTaskPayload = this.payloadList.get(taskPayloadNo);
			if(nowSegmentCapacity <= 0)
			{
				++taskSegNo;
				if(remainingCapacity - AppPacketDef.RANGE_PAYLOAD < 0)
				{
					nowTaskArray = new byte[remainingCapacity + AppPacketDef.PACKET_METADATA_SIZE];
				}
				else
				{
					//제어 문자가 끝에 오지 않도록 해주는 로직 작성.
					int findEndCount = AppPacketDef.RANGE_PAYLOAD - 1;
					int findEndPayloadNo = 0;
					int nowSize = this.payloadList.get(taskPayloadNo).length - nowPayloadPointer;
					while(true)
					{
						if(findEndCount - nowSize <= 0)
						{
							break;
						}
						++findEndPayloadNo;
						findEndCount -= nowSize;
						nowSize = this.payloadList.get(taskPayloadNo + findEndPayloadNo).length;
					}
					if(this.payloadList.get(taskPayloadNo + findEndPayloadNo)[findEndCount + (findEndPayloadNo <= 0 ? nowPayloadPointer:0)] == AppPacketDef.CONTROL_MARK)
					{
						nowTaskArray = new byte[AppPacketDef.MAX_SEGMENT_SIZE - 1];
					}
					else
					{
						nowTaskArray = new byte[AppPacketDef.MAX_SEGMENT_SIZE];
					}
				}
				
				ByteBuffer buf = ByteBuffer.wrap(nowTaskArray);
				buf.put(AppPacketDef.CONTROL_START);
				buf.putShort((short)(this.channelID + AppPacketDef.ADD_NUMBER_FIELD));
				buf.putShort(this.option);
				//buf.putShort((short)(segmentCount + AppPacketDef.ADD_NUMBER_FIELD));
				buf.putShort((short)(taskSegNo + AppPacketDef.ADD_NUMBER_FIELD));
				buf.position(nowTaskArray.length - AppPacketDef.CONTROL_END.length);
				buf.put(AppPacketDef.CONTROL_END);
				
				assembledPacket.add(nowTaskArray);
			}
			
			
			int capPayload = nowTaskPayload.length - nowPayloadPointer;
			int capSegment = nowTaskArray.length - AppPacketDef.PACKET_METADATA_SIZE - nowSegmentCapacity;
			int storeLength = capPayload < capSegment ? capPayload : capSegment;
			System.arraycopy(nowTaskPayload, nowPayloadPointer, 
					nowTaskArray, nowSegmentCapacity + AppPacketDef.PACKET_METADATA_SIZE - AppPacketDef.RANGE_CONTROL_END, storeLength);
			remainingCapacity -= storeLength;
			nowPayloadPointer += storeLength;
			nowSegmentCapacity += storeLength;
			
			if(nowSegmentCapacity >= nowTaskArray.length - AppPacketDef.PACKET_METADATA_SIZE)
			{
				nowSegmentCapacity = 0;
				++taskSegNo;
			}
			
			if(nowPayloadPointer >= nowTaskPayload.length)
			{
				nowPayloadPointer = 0;
				++taskPayloadNo;
				if(taskPayloadNo >= this.payloadList.size())
				{
					break;
				}
			}
		}
		byte[][] rawPacket = new byte[assembledPacket.size()][];
		assembledPacket.toArray(rawPacket);
		
		AppPacket appPacket = new AppPacket(rawPacket);
		
		return appPacket;
	}
	
	public short getChannelID()
	{
		return this.channelID;
	}
}
