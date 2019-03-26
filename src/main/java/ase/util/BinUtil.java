package ase.util;

public class BinUtil
{
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static final byte[] intToByteArray(int value)
	{
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	
	public static byte[] shortToByteArray(short value)
	{
		return new byte[] {(byte)(value >>> 8), (byte)(value)};
	}
	
	public static String bytesToHex(byte[] bytes,int start, int end)
	{
		StringBuffer buf = new StringBuffer();
		for (int j = start; j < end; j++)
		{
			int v = bytes[j] & 0xFF;
			buf.append(hexArray[v >>> 4]);
			buf.append(hexArray[v & 0x0F]);
			buf.append(' ');
		}
		return buf.toString();
	}
	
	public static String bytesToHex(byte[] bytes)
	{
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			buf.append(hexArray[v >>> 4]);
			buf.append(hexArray[v & 0x0F]);
			buf.append(' ');
		}
		return buf.toString();
	}
}
