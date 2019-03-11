package ase.appConnect.channel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AppDataPacketAnalyser
{
	public final byte[][] payload;
	private final int[] payloadSize;
	private final InputStream input;
	
	private int nowTaskPaylaod;
	
	public AppDataPacketAnalyser(byte option, InputStream input) throws IOException
	{// 이전 레벨에서 옵션 필드, 채널 필드 확인
		this.input = input;
		
		ByteBuffer buf;
		int payloadCount;
		
		byte[] readData = ProtocolDefine.fillBuffer(this.input, ProtocolDefine.RANGE_PAYLOAD_DATALEN);
		buf = ByteBuffer.wrap(readData);
		payloadCount = buf.getInt();
		
		if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_PAYLOAD_SINGLE))
		{// 한개의 페이로드만 있을 때
			this.payload = new byte[1][];
			this.payloadSize = new int[1];
			this.payloadSize[0] = payloadCount;
		}
		else
		{// 복수의 페이로드가 있을 때
			this.payload = new byte[payloadCount][];
			this.payloadSize = new int[payloadCount];
			for(int i = 0; i < payloadCount; ++i)
			{
				buf = ByteBuffer.wrap(ProtocolDefine.fillBuffer(this.input, ProtocolDefine.RANGE_PAYLOAD_DATALEN));
				this.payloadSize[i] = buf.getInt();
			}
		}
		
		this.nowTaskPaylaod = 0;
	}
	
	public boolean readData() throws IOException
	{// 읽기를 전부 완료하면 true 반환
		this.payload[this.nowTaskPaylaod] = ProtocolDefine.fillBuffer(this.input, this.payloadSize[this.nowTaskPaylaod]);
		++this.nowTaskPaylaod;
		if(this.nowTaskPaylaod >= this.payloadSize.length)
		{
			return true;
		}
		return false;
	}
}
