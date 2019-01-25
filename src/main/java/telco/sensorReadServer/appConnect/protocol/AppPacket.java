package telco.sensorReadServer.appConnect.protocol;

public class AppPacket
{
	private final byte[][] rawData;
	
	public AppPacket(byte[][] rawData)
	{
		this.rawData = rawData;
	}
	
	public short getChannelID()
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.rawData.length; ++i)
		{
			buf.append(AppPacketDef.bytesToHex(this.rawData[i], this.rawData[i].length));
			buf.append(" (");
			buf.append(this.rawData[i].length);
			buf.append(")\n");
		}
		return buf.toString();
	}
}
