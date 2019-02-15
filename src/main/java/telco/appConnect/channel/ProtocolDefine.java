package telco.appConnect.channel;

import java.io.IOException;
import java.io.InputStream;

public class ProtocolDefine
{
	/*
	 * 프로토콜 정의
	 * 1. 채널 열기: 옵션1/채널2/키길이4/키
	 * 2. 채널 닫기: 옵션1/채널2
	 * 3. 채널 데이타: 옵션1/채널2/데이타개수4/데이타길이4/데이타길이4.../페이로드
	 * 4. 채널 데이타 싱글: 옵션1/채널2/데이타길이4/페이로드
	 * 5. 소켓 정상종료: 옵션1
	 * 6. 소켓 정상오픈: 이것저것
	 * 7. 일반 데이타: 옵션1/키길이4/키/데이타길이4/데이타
	 * 8. 일반 데이타 싱글: 옵션1/키길이4/키/데이타개수4/데이타길이4.../페이로드
	 * 
	 */
	
	public static final byte[] CONTROL_SOCKET_STX = new byte[] { 0x11, 0x22, 0x33, 0x44};
	
	public static final byte OPTION_CHANNEL = 0b01000000;
	public static final byte OPTION_CHANNEL_OPEN = 0b00100000;
	public static final byte OPTION_CHANNEL_CLOSE = 0b00010000;
	public static final byte OPTION_PAYLOAD = 0b00001000;
	public static final byte OPTION_PAYLOAD_SINGLE = 0b00000100;
	public static final byte OPTION_SOCKET_CLOSE = 0b00000010;
	
	public static final int RANGE_CHANNEL_PAYLOAD_HEADER = 7;
	public static final int RANGE_PAYLOAD_DATALEN = 4;
	
	public static final int START_OPTION = 0;
	public static final int RANGE_OPTION = 1;
	public static final int END_OPTION = 1;
	public static final int START_CHANNEL = 1;
	public static final int RANGE_CHANNEL = 2;
	public static final int END_CHANNEL = 3;
	
	public static final short CHANNEL_ASSIGN_SERVER = 1;
	public static final short CHANNEL_ASSIGN_CLIENT = 2;
	
	public static final short MAX_CHANNEL_COUNT = 1024;
	public static final short CHANNEL_ASSIGN_ORDER = CHANNEL_ASSIGN_SERVER;
	
	public static String printOption(byte option)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("OPTION_CHANNEL = ");
		buf.append(checkOption(option, OPTION_CHANNEL));
		buf.append('\n');
		buf.append("OPTION_CHANNEL_OPEN = ");
		buf.append(checkOption(option, OPTION_CHANNEL_OPEN));
		buf.append('\n');
		buf.append("OPTION_CHANNEL_CLOSE = ");
		buf.append(checkOption(option, OPTION_CHANNEL_CLOSE));
		buf.append('\n');
		buf.append("OPTION_PAYLOAD = ");
		buf.append(checkOption(option, OPTION_PAYLOAD));
		buf.append('\n');
		buf.append("OPTION_PAYLOAD_SINGLE = ");
		buf.append(checkOption(option, OPTION_PAYLOAD_SINGLE));
		buf.append('\n');
		buf.append("OPTION_SOCKET_CLOSE = ");
		buf.append(checkOption(option, OPTION_SOCKET_CLOSE));
		buf.append('\n');
		return buf.toString();
	}
	
	public static byte[] fillBuffer(InputStream input, int size) throws IOException
	{
		byte[] buffer = new byte[size];
		int readPointer = 0;

		while (readPointer < size)
		{
			int readCount = input.read(buffer, readPointer, size - readPointer);
			if(readCount <= -1)
			{
				throw new IOException("데이터 패킷 분석중 오류3");
			}
			readPointer += readCount;
		}
		return buffer;
	}
	
	public static final byte[] intToByteArray(int value)
	{
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	
	public static byte[] shortToByteArray(short value)
	{
		return new byte[] {(byte)(value >>> 8), (byte)(value)};
	}
	
	
	
	/*public static final int CONTROL_SEQ_SIZE = 3;
	public static final byte[] CONTROL_MARK = new byte[] {-0x7D, -0x7E};
	public static final byte[] CONTROL_START = new byte[] { CONTROL_MARK[0], CONTROL_MARK[1], 0x00};
	public static final byte[] CONTROL_END = new byte[] { CONTROL_MARK[0], CONTROL_MARK[1], 0x01};
	public static final byte[] CONTROL_DATA_ESCAPE = new byte[] { CONTROL_MARK[0], CONTROL_MARK[1], 0x10};
	public static final byte[] CONTROL_DATA_SEPARATOR = new byte[] { CONTROL_MARK[0], CONTROL_MARK[1], 0x11};
	public static final byte[] CONTROL_DATA_EVENTKEY = new byte[] { CONTROL_MARK[0], CONTROL_MARK[1], 0x12};
	
	//public static final short MAX_SEGMENT_SIZE = 16384;
	public static final short MAX_SEGMENT_SIZE = 32;
	
	public static final int START_CONTROL_START = 0;
	public static final int RANGE_CONTROL_START = 3;
	public static final int START_PACKET_CHANNEL = 3;
	public static final int RANGE_PACKET_CHANNEL = 2;
	public static final int START_PACKET_FULLSEG = 5;
	public static final int RANGE_PACKET_FULLSEG = 2;
	public static final int START_PACKET_NUM = 7;
	public static final int RANGE_PACKET_NUM = 2;

	public static final int RANGE_CONTROL_END = 3;

	public static final int PACKET_METADATA_SIZE = RANGE_CONTROL_START + RANGE_PACKET_CHANNEL
			+ RANGE_PACKET_FULLSEG + RANGE_PACKET_NUM + RANGE_CONTROL_END;
	public static final int START_PAYLOAD = RANGE_CONTROL_START + RANGE_PACKET_CHANNEL
			+ RANGE_PACKET_FULLSEG + RANGE_PACKET_NUM;
	public static final int RANGE_PAYLOAD = MAX_SEGMENT_SIZE - PACKET_METADATA_SIZE;
	public static final int FULL_PACKET_LIMIT = 1024 * 1024 * 100;// 100mbyte제한
	*/
	public static boolean checkOption(byte optionArea, byte option)
	{
		if((optionArea & option) != 0)
		{
			return true;
		}
		return false;
	}
	
	public static byte writeOption(byte optionArea, byte option)
	{
		optionArea = (byte)(optionArea | option);
		return optionArea;
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes, int end)
	{
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < end; j++)
		{
			int v = bytes[j] & 0xFF;
			buf.append(hexArray[v >>> 4]);
			buf.append(hexArray[v & 0x0F]);
			buf.append(' ');
		}
		return buf.toString();
	}
}
