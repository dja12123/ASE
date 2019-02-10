package telco.sensorReadServer.appConnect;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.sensorReadServer.console.LogWriter;

public class Channel
{
	public static final Logger logger = LogWriter.createLogger(Channel.class, "channel");
	
	public final short id;
	public final String key;
	private final OutputStream output;
	
	private boolean isOpen;
	private ChannelUser user;
	
	public Channel(short id, String key, OutputStream output)
	{
		this.id = id;
		this.key = key;
		this.output = output;
		this.user = null;
		this.isOpen = true;
	}
	
	public AppDataPacketBuilder getPacketBuilder()
	{
		return new AppDataPacketBuilder(this.id);
	}
	
	public void sendData(AppDataPacketBuilder builder)
	{
		if(this.closeCheck("sendData")) return;
		
		byte[] metadata = builder.getMetadata();
		byte[][] allPayload = builder.getPayload();

		synchronized (this.output)
		{
			try
			{
				this.output.write(metadata);
				for(byte[] payload : allPayload)
				{
					this.output.write(payload);
				}
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, this.id + ": 전송중 오류 ", e);
			}
		}
	}
	
	public void receiveTask(AppDataPacketAnalyser analyser)
	{
		if(this.closeCheck("receiveTask")) return;
		try
		{
			
			while(!analyser.readData())
			{
				
			}
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.id + ": 수신중 오류 ", e);
		}
		
		if(this.user != null)
		{
			this.user.receiveData(this, analyser.payload);
		}
	}
	
	public void setUser(ChannelUser user)
	{
		if(this.closeCheck("setReceiveCallback")) return;
		
		this.user = user;
	}
	
	public boolean isOpen()
	{
		return this.isOpen;
	}
	
	void alertClose()
	{
		if(this.closeCheck("alertClose")) return;
		
		if(this.user != null)
		{
			this.user.closeChannel(this);
		}
		
		this.isOpen = false;
	}
	
	private boolean closeCheck(String func)
	{
		if(!this.isOpen)
		{
			logger.log(Level.SEVERE, this.id + ": 채널은 이미 닫힘 " + func);
			return true;
		}
		return false;
	}
}
