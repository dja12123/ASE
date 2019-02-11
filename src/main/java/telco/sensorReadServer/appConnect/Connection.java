package telco.sensorReadServer.appConnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.sensorReadServer.console.LogWriter;

public class Connection
{
	public static final Logger logger = LogWriter.createLogger(Connection.class, "Connection");
	
	private final Socket socket;
	private final ConnectionUser connectionUser;
	private final HashMap<Short, Channel> channels;
	private final boolean assignID[];
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean isRun;
	private Thread receiveThread;

	public Connection(Socket socket, ConnectionUser connectionUser)
	{	
		this.socket = socket;
		this.connectionUser = connectionUser;
		this.channels = new HashMap<Short, Channel>();
		this.assignID = new boolean[ProtocolDefine.MAX_CHANNEL_COUNT];
	}
	
	public synchronized boolean startConnection()
	{
		if(this.isRun) return false;
		this.isRun = true;
		
		try
		{
			this.inputStream = this.socket.getInputStream();
			this.outputStream = this.socket.getOutputStream();
			/*
			OutputStream stream = this.socket.getOutputStream();
			this.outputStream = new OutputStream()
					{
				
						@Override
						public void close() throws IOException
						{
							stream.close();
						}
				
						@Override
						public void write(byte[] b) throws IOException
						{
							stream.write(b);
							System.out.println(ProtocolDefine.bytesToHex(b, b.length));
						}
						@Override
						public void write(byte[] b, int off, int len) throws IOException
						{
							// TODO Auto-generated method stub
							stream.write(b, off, len);
						}
						@Override
						public void write(int b) throws IOException
						{
							stream.write(b);
							System.out.println(ProtocolDefine.bytesToHex(new byte[] {(byte) b}, 1));
						}
					
					};*/
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "소켓 io스트림 가져오기 오류", e);
			return false;
		}
		
		try
		{
			this.outputStream.write(ProtocolDefine.CONTROL_SOCKET_STX);
			logger.log(Level.INFO, "stx전송완료");
		}
		catch (IOException e1)
		{
			logger.log(Level.WARNING, "stx송신중 오류");
		}
		
		byte[] stxSig;
		try
		{
			logger.log(Level.INFO, "stx수신대기");
			stxSig = ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.CONTROL_SOCKET_STX.length);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "stx수신중 오류", e);
			return false;
		}
		if(!Arrays.equals(stxSig, ProtocolDefine.CONTROL_SOCKET_STX))
		{
			logger.log(Level.WARNING, "stx가 아님");
			return false;
		}
		
		for(int i = 0; i < this.assignID.length; ++i)
		{
			this.assignID[i] = false;
		}
		
		this.isRun = true;
		this.receiveThread = new Thread(this::receiveData, "connection");
		this.receiveThread.start();
		return true;
	}
	
	public synchronized void closeConnection()
	{
		if(!this.isRun) return;
		this.isRun = false;
		ArrayList<Channel> removeList = new ArrayList<Channel>();
		removeList.addAll(this.channels.values());
		for(Channel channel : removeList)
		{
			this.closeChannel(channel);
		}
		
		try
		{
			synchronized (this.outputStream)
			{
				this.outputStream.write(ProtocolDefine.OPTION_SOCKET_CLOSE);
			}

		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "etx송신중 오류", e);
		}
		
		this.connectionUser.closeConnection(this);
	}
	
	public void closeForce()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "소켓 종료중 오류", e);
		}
		this.connectionUser.closeConnection(this);
	}
	
	private void receiveData()
	{
		byte[] buffer = new byte[ProtocolDefine.RANGE_OPTION];
		int readSize;
		while(true)
		{
			try
			{
				readSize = this.inputStream.read(buffer);
				if(!this.isRun)
				{
					return;
				}
			
				if(readSize != 1)
				{
					logger.log(Level.WARNING, "소켓 읽기중 오류1: " + readSize);
					this.closeForce();
					return;
				}
				
				if(ProtocolDefine.checkOption(buffer[0], ProtocolDefine.OPTION_CHANNEL))
				{
					this.channelReceive(buffer[0]);
				}
				else if(ProtocolDefine.checkOption(buffer[0], ProtocolDefine.OPTION_SOCKET_CLOSE))
				{
					this.closeForce();
					return;
				}
				else
				{
					logger.log(Level.WARNING, "소켓 읽기중 오류2 " + buffer[0]);
					this.closeForce();
					return;
				}
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, "소켓 읽기중 오류3", e);
				this.closeForce();
				return;
			}
			
		}
	}
	
	private void channelReceive(byte option) throws IOException
	{
		ByteBuffer buf = ByteBuffer.wrap(ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.RANGE_CHANNEL));
		short id = buf.getShort();
		
		if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_CHANNEL_PAYLOAD))
		{
			AppDataPacketAnalyser packetAnalyser = new AppDataPacketAnalyser(option, this.inputStream);
			if(this.channels.containsKey(id))
			{
				Channel channel = this.channels.get(id);
				channel.receiveTask(packetAnalyser);
			}
			else
			{
				logger.log(Level.WARNING, "데이터가 왔는데 해당하는 채널이 없음");
				this.closeForce();
				return;
			}
		}
		else if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_CHANNEL_OPEN))
		{
			buf = ByteBuffer.wrap(ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.RANGE_CHANNEL_PAYLOAD_DATALEN));
			int keySize = buf.getInt();
			String key = new String(ProtocolDefine.fillBuffer(this.inputStream, keySize));
			Channel channel = new Channel(id, key, this.outputStream);
			this.channels.put(id, channel);
			this.connectionUser.createChannel(this, channel);
		}
		else if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_CHANNEL_CLOSE))
		{
			if(this.channels.containsKey(id))
			{
				Channel channel = this.channels.get(id);
				channel.alertClose();
				this.assignID[channel.id] = false;
				this.channels.remove(id);
			}
			else
			{
				logger.log(Level.WARNING, "닫기를 시도하는 채널이 없음");
				this.closeForce();
				return;
			}
		}
	}
	
	public Channel channelOpen(String key)
	{
		short id = this.findEmptyChannel();
		this.assignID[id] = true;
		
		if(id == -1)
		{
			logger.log(Level.SEVERE, "더이상 채널 ID를 할당할 수 없음");
			this.closeForce();
			return null;
		}
		Channel channel = new Channel(id, key, this.outputStream);
		if(channel.sendChannelOpenMsg(id, key))
		{
			this.channels.put(id, channel);
			this.connectionUser.createChannel(this, channel);
			return channel;
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private short findEmptyChannel()
	{
		if(ProtocolDefine.CHANNEL_ASSIGN_ORDER == ProtocolDefine.CHANNEL_ASSIGN_CLIENT)
		{
			for(int i = 0; i < this.assignID.length; ++i)
			{
				if(!this.assignID[i])
				{
					return (short)i;
				}
			}
		}
		else
		{
			for(int i = this.assignID.length - 1; i >=0 ; --i)
			{
				if(!this.assignID[i])
				{
					return (short)i;
				}
			}
		}
		return -1;
	}
	
	public void closeChannel(Channel channel)
	{
		this.assignID[channel.id] = false;
		this.channels.remove(channel.id);

		channel.sendChannelCloseMessage();
	}
	

	
}