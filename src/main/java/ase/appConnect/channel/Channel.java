package ase.appConnect.channel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.util.BinUtil;
import ase.util.observer.Observable;

public class Channel
{
	public static final Logger logger = LogWriter.createLogger(Channel.class, "channel");
	
	public static final int STATE_NORMAL = 1;
	public static final int STATE_CLOSEING = 2;
	public static final int STATE_CLOSE = 3;
	
	public final short id;
	public final String key;
	private final OutputStream output;
	
	private boolean isOpen;
	private ChannelReceiveCallback recvCallback;
	private Observable<ChannelEvent> openCloseProvider;
	
	public Channel(short id, String key, OutputStream output, Observable<ChannelEvent> openCloseProvider)
	{
		this.id = id;
		this.key = key;
		this.output = output;
		this.recvCallback = null;
		this.openCloseProvider = openCloseProvider;
		this.isOpen = true;
	}
	
	public boolean sendChannelOpenMsg(short id, String key)
	{
		byte option = ProtocolDefine.OPTION_CHANNEL;
		option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_CHANNEL_OPEN);
		logger.log(Level.INFO, this.toString()+" 채널 열기 전송");
		try
		{
			synchronized (this.output)
			{
				this.output.write(option);
				this.output.write(BinUtil.shortToByteArray(id));
				this.output.write(BinUtil.intToByteArray(key.length()));
				this.output.write(key.getBytes());
			}
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" 채널 여는중 오류", e);
			return false;
		}
		return true;
	}
	
	public void sendChannelCloseMessage()
	{
		byte option = ProtocolDefine.OPTION_CHANNEL;
		option = ProtocolDefine.writeOption(option, ProtocolDefine.OPTION_CHANNEL_CLOSE);
		
		try
		{
			synchronized (this.output)
			{
				this.output.write(option);
				this.output.write(BinUtil.shortToByteArray(this.id));
			}
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+ " 채널 닫기 오류 ", e);
		}
		
		this.alertClose();
		
	}
	
	public void sendData(AppDataPacketBuilder builder)
	{
		if(this.closeCheck("sendData")) return;
		
		byte option = ProtocolDefine.writeOption((byte) 0, ProtocolDefine.OPTION_CHANNEL);
		option = builder.writeOption(option);
		byte[][] allPayload = builder.getPayload();
		synchronized (this.output)
		{
			try
			{
				this.output.write(option);
				this.output.write(BinUtil.shortToByteArray(this.id));
				for(byte[] payload : allPayload)
				{
					this.output.write(payload);
				}
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, this.toString()+ " 전송중 오류 ", e);
			}
		}
	}
	
	public void receiveTask(AppDataPacketAnalyser analyser)
	{
		if(this.closeCheck("receiveTask")) return;
		try
		{
			while(!analyser.readData());
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+ ":수신중 오류 ", e);
		}
		
		if(this.recvCallback != null)
		{
			this.recvCallback.receiveData(this, analyser.payload);
		}
	}
	
	public void setReceiveCallback(ChannelReceiveCallback callback)
	{
		if(this.closeCheck("setReceiveCallback")) return;
		
		this.recvCallback = callback;
	}
	
	public boolean isOpen()
	{
		return this.isOpen;
	}
	
	public void alertClose()
	{
		if(this.closeCheck("alertClose")) return;
		
		this.isOpen = false;
		this.openCloseProvider.notifyObservers(new ChannelEvent(this, false));
	}
	
	private boolean closeCheck(String func)
	{
		if(!this.isOpen)
		{
			throw new RuntimeException(this.toString()+" 채널은 이미 닫힘 " + func);
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		String str = "ch"+this.id+"("+this.key+")";
		return str;
		
	}
}
