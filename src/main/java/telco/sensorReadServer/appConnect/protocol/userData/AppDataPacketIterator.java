package telco.sensorReadServer.appConnect.protocol.userData;

import java.util.ArrayList;
import java.util.Iterator;

import telco.sensorReadServer.appConnect.protocol.ProtocolDefine;
import telco.sensorReadServer.appConnect.protocol.rawData.AppRawPacket;
import telco.sensorReadServer.appConnect.protocol.rawData.AppRawPacketBuilder;

public class AppDataPacketIterator implements Iterator<byte[]>
{
	private ArrayList<byte[]> data;
	private int position;
	
	public static void main(String[] args) throws Exception
	{
		AppDataPacketBuilder builder = new AppDataPacketBuilder();
		byte[] t1 = new byte[]{-0x7D,-0x7E,0x00,-0x7D,-0x7E,0x01,0x02,0x03,0x04,0x05,-0x7D,-0x7D,-0x7E,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x10,0x11,0x12,-0x7D,-0x7E,0x13,0x14,-0x7E,-0x7E};
		
		
		byte[] t2 = new byte[]{-0x7D,-0x7E,0x00,0x01,0x02,0x03,0x04,0x05,0x06};
		byte[] t3 = new byte[]{-0x7D,-0x7E,0x00,0x01,0x02,0x03,0x04,0x05,0x06};
		byte[] t4 = new byte[]{-0x7D,-0x7E,0x00,0x01,0x02,0x03,0x04,0x05,0x06};

		for(int j = 0; j < 1000; ++j)
		{
			long start = System.currentTimeMillis();
			builder.appendData(t1);
			builder.appendData(t1);
			builder.appendData(t1);
			builder.appendData(t1);
			builder.appendData(t1);
			builder.appendData(t2);
			builder.appendData(t3);
			builder.appendData(t4);
			byte[] b = builder.createData();
			AppRawPacket raw = new AppRawPacket((short) 0x0101, b);
			byte[][] rawbytes = raw.getRawData();
			AppRawPacketBuilder rawBuilder = new AppRawPacketBuilder(rawbytes[0]);
			for(int i = 1; i < rawbytes.length; ++i)
			{
				rawBuilder.appendData(rawbytes[i]);
			}
			byte[] repayload = rawBuilder.getPayload();
			AppDataPacketIterator reitr = new AppDataPacketIterator(repayload);
			ArrayList<byte[]> finalData = new ArrayList<byte[]>();
			while(reitr.hasNext())
			{
				finalData.add(reitr.next());
			}
			
			for(byte[] data : finalData)
			{
				//System.out.println(ProtocolDefine.bytesToHex(data, data.length));
			}
			long end = System.currentTimeMillis() - start;
			System.out.println(end);
		}
		
	}
	public AppDataPacketIterator(byte[] rawData)
	{
		this.data = new ArrayList<byte[]>();
		this.position = 0;
		
		int matchSeparator = 0;
		int segmentSize = 0;
		int getPosition = 0;
		
		for(int i = 0; i < rawData.length; ++i)
		{
			++segmentSize;
			if(rawData[i] == ProtocolDefine.CONTROL_DATA_SEPARATOR[matchSeparator])
			{
				++matchSeparator;
				if(matchSeparator >= ProtocolDefine.CONTROL_DATA_SEPARATOR.length)
				{
					byte[] segRawData = new byte[segmentSize - ProtocolDefine.CONTROL_DATA_SEPARATOR.length];
					this.data.add(segRawData);
					System.arraycopy(rawData, getPosition, segRawData, 0, segmentSize - ProtocolDefine.CONTROL_DATA_SEPARATOR.length);
					matchSeparator = 0;
					getPosition += segmentSize;
					segmentSize = 0;
				}
			}
			else
			{
				matchSeparator = 0;
			}
		}
	}

	@Override
	public boolean hasNext()
	{
		if(this.position >= data.size())
		{
			return false;
		}
		return true;
	}

	@Override
	public byte[] next()
	{
		byte[] rawSegment = this.data.get(this.position++);
		ArrayList<Integer> escapePosition = getControlMarkEscapePosition(rawSegment);
		if(escapePosition.size() > 0)
		{
			int beforeMark = 0;
			byte[] convertEscapeSegment = new byte[rawSegment.length - (escapePosition.size() * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))];
			for(int i = 0; i < escapePosition.size(); ++i)
			{
				System.arraycopy(rawSegment, beforeMark, convertEscapeSegment, beforeMark - (i * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
						, escapePosition.get(i) - beforeMark);
				beforeMark = escapePosition.get(i);
			}
			System.arraycopy(rawSegment, beforeMark, convertEscapeSegment, beforeMark - (escapePosition.size() * (ProtocolDefine.CONTROL_SEQ_SIZE - ProtocolDefine.CONTROL_MARK.length))
					, rawSegment.length - beforeMark);
			rawSegment = convertEscapeSegment;
		}
		return rawSegment;
	}
	
	public String nextString()
	{
		return new String(this.next());
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(byte[] segment : this.data)
		{
			buf.append(ProtocolDefine.bytesToHex(segment, segment.length));
			buf.append(" (");
			buf.append(segment.length);
			buf.append(")\n");
		}
		return buf.toString();
	}
	
	private static ArrayList<Integer> getControlMarkEscapePosition(byte[] payload)
	{
		int controlMarkCheck = 0;
		ArrayList<Integer> removePosition = new ArrayList<Integer>();
		
		for(int i = 0; i < payload.length; ++i)
		{
			if(payload[i] == ProtocolDefine.CONTROL_DATA_ESCAPE[controlMarkCheck])
			{
				++controlMarkCheck;
				if(controlMarkCheck >= ProtocolDefine.CONTROL_MARK.length)
				{
					removePosition.add(i + ProtocolDefine.CONTROL_MARK.length);
					controlMarkCheck = 0;
				}
			}
			else
			{
				if(payload[i] == ProtocolDefine.CONTROL_DATA_ESCAPE[0]) controlMarkCheck = 1;
				else controlMarkCheck = 0;
			}
		}
		return removePosition;
	}
}
