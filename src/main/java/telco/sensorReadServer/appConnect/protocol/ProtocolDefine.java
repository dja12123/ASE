package telco.sensorReadServer.appConnect.protocol;

public class ProtocolDefine
{
	public static final int CONTROL_SEQ_SIZE = 3;
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
	
	public static boolean checkOption(short optionArea, short option)
	{
		if((optionArea & option) != 0)
		{
			return true;
		}
		return false;
	}
	
	public static short writeOption(short optionArea, short option)
	{
		optionArea = (short)(optionArea | option);
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
