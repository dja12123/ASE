package telco.sensorReadServer.appConnect.protocol;

import java.util.LinkedList;
import java.util.List;

public class AppRawPacketBuilder
{
	public static void main(String[] args) throws Exception
	{
		AppRawPacketBuilder builder = new AppRawPacketBuilder((short) 123);
		builder.appendData(new byte[]{0x11, 0x11, 0x11,0x11, 0x11, 0x10,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11,0x11, 0x11, 0x11});
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
		{
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
					nowTaskArray = new byte[AppPacketDef.MAX_SEGMENT_SIZE];
				}
				assembledPacket.add(nowTaskArray);
			}
			
			/*if(assembledPacket[taskSegNo] == null)
			{
				if(remainingCapacity >= AppPacketDef.RANGE_PAYLOAD)
				{
					assembledPacket[taskSegNo] = new byte[AppPacketDef.MAX_SEGMENT_SIZE];
				}
				else
				{
					assembledPacket[taskSegNo] = new byte[remainingCapacity + AppPacketDef.PACKET_METADATA_SIZE];
				}
				ByteBuffer buf = ByteBuffer.wrap(assembledPacket[taskSegNo]);
				buf.put(AppPacketDef.CONTROL_START);
				buf.putShort((short)(this.channelID + AppPacketDef.ADD_NUMBER_FIELD));
				buf.putShort(this.option);
				buf.putShort((short)(segmentCount + AppPacketDef.ADD_NUMBER_FIELD));
				buf.putShort((short)(taskSegNo + AppPacketDef.ADD_NUMBER_FIELD));
				buf.position(assembledPacket[taskSegNo].length - AppPacketDef.CONTROL_END.length);
				buf.put(AppPacketDef.CONTROL_END);
			}*/
			
			
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
