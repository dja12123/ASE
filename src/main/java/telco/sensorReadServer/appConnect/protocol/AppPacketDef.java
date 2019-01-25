package telco.sensorReadServer.appConnect.protocol;

public class AppPacketDef
{
	public static final byte CONTROL_MARK = 0x11;
	public static final byte[] CONTROL_START = new byte[] { CONTROL_MARK, 0x01 };
	public static final byte[] CONTROL_END = new byte[] { CONTROL_MARK, 0x02 };
	public static final byte[] CONTROL_DATA_SEPARATOR = new byte[] {CONTROL_MARK, 0x12};
	public static final byte[] CONTROL_DATA_TO_ZERO = new byte[] {CONTROL_MARK, 0x21};
	// 데이터 분석시 혼동을 피하기 위해 데이터를 삽입할 때 0x7F을 0x0021로 항상 변환.
	//0x1111은 금지 코드
	
	public static final short OPT_KEY_PACKET = 0b0100000000000000;
	public static final short OPT_DATA_PACKET = 0b0010000000000000;
	
	public static final int CONTROL_SEQ_SIZE = 2;
	
	public static final short ADD_NUMBER_FIELD = (CONTROL_MARK + 1) << 8;
	// 제어 문자와 혼동을 피하기 위해 숫자 필드에 해당 값을 더해줌.

	//public static final short MAX_SEGMENT_SIZE = 16384;
	public static final short MAX_SEGMENT_SIZE = 32;
	
	public static final int START_CONTROL_START = 0;
	public static final int RANGE_CONTROL_START = 2;
	public static final int START_PACKET_ID = 2;
	public static final int RANGE_PACKET_ID = 2;
	public static final int START_PACKET_OPTION = 4;
	public static final int RANGE_PACKET_OPTION = 2;
	public static final int START_PACKET_FULLSEG = 6;
	public static final int RANGE_PACKET_FULLSEG = 2;
	public static final int START_PACKET_NUM = 8;
	public static final int RANGE_PACKET_NUM = 2;

	public static final int RANGE_CONTROL_END = 2;

	public static final int PACKET_METADATA_SIZE = RANGE_CONTROL_START + RANGE_PACKET_ID
			+ RANGE_PACKET_OPTION + RANGE_PACKET_FULLSEG + RANGE_PACKET_NUM + RANGE_CONTROL_END;
	public static final int START_PAYLOAD = RANGE_CONTROL_START + RANGE_PACKET_ID
			+ RANGE_PACKET_OPTION + RANGE_PACKET_FULLSEG + RANGE_PACKET_NUM;
	public static final int RANGE_PAYLOAD = MAX_SEGMENT_SIZE - PACKET_METADATA_SIZE;
	public static final int FULL_PACKET_LIMIT = 1024 * 1024 * 100;// 100mbyte제한
	
	public static boolean checkOption(short optionArea, int option)
	{
		int checkPointer = 0b0100000000000000 >> (option - 1);
		if((optionArea & checkPointer) != 0)
		{
			return true;
		}
		return false;
	}
	
	public static short writeOption(short optionArea, int option)
	{
		int mask = 0b0100000000000000 >> (option - 1);
		optionArea = (short)(optionArea | mask);
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
