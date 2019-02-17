package telco.appConnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.channel.AppDataPacketAnalyser;
import telco.appConnect.channel.AppDataPacketBuilder;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ChannelEvent;
import telco.appConnect.channel.ProtocolDefine;
import telco.console.LogWriter;
import telco.util.observer.Observable;

public class Connection extends Observable<ChannelEvent>
{
	public static final Logger logger = LogWriter.createLogger(Connection.class, "Connection");
	
	private final Socket socket;
	private Observable<ConnectionStateChangeEvent> connectionStateChangeObservable;
	private final HashMap<Short, Channel> channels;
	private final boolean assignID[];
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean isRun;
	private Thread receiveThread;
	
	private GeneralDataReceiveCallback generalDataReceiveCallback;

	public Connection(Socket socket, Observable<ConnectionStateChangeEvent> connectionStateChangeObservable)
	{
		this.socket = socket;
		this.connectionStateChangeObservable = connectionStateChangeObservable;
		this.channels = new HashMap<Short, Channel>();
		this.assignID = new boolean[ProtocolDefine.MAX_CHANNEL_COUNT];
		this.generalDataReceiveCallback = null;
	}
	
	public synchronized boolean startConnection()
	{
		if(this.isRun) return false;
		this.isRun = true;
		
		try
		{
			this.inputStream = this.socket.getInputStream();
			this.outputStream = this.socket.getOutputStream();
			
			/*OutputStream stream = this.socket.getOutputStream();
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
			logger.log(Level.WARNING, this.toString()+" 소켓 io스트림 가져오기 오류", e);
			return false;
		}
		
		try
		{
			this.outputStream.write(ProtocolDefine.CONTROL_SOCKET_STX);
			logger.log(Level.INFO, this.toString()+" stx전송완료");
		}
		catch (IOException e1)
		{
			logger.log(Level.WARNING, this.toString()+" stx송신중 오류");
		}
		
		byte[] stxSig;
		try
		{
			logger.log(Level.INFO, this.toString()+" stx수신대기");
			stxSig = ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.CONTROL_SOCKET_STX.length);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" stx수신중 오류", e);
			return false;
		}
		if(!Arrays.equals(stxSig, ProtocolDefine.CONTROL_SOCKET_STX))
		{
			logger.log(Level.WARNING, this.toString()+" stx가 아님");
			return false;
		}
		
		for(int i = 0; i < this.assignID.length; ++i)
		{
			this.assignID[i] = false;
		}
		
		this.isRun = true;
		this.receiveThread = new Thread(this::receiveData, "connection");
		this.receiveThread.setDaemon(true);
		this.receiveThread.start();
		this.connectionStateChangeObservable.notifyObservers(new ConnectionStateChangeEvent(this, true));
		return true;
	}
	
	public synchronized void closeSafe()
	{
		if(this.closeCheck("closeSafe")) return;
		
		
		this.closeAllChannel();
		
		try
		{
			synchronized (this.outputStream)
			{
				this.outputStream.write(ProtocolDefine.OPTION_SOCKET_CLOSE);
			}
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" etx송신중 오류", e);
		}
		
		this.isRun = false;
		this.connectionStateChangeObservable.notifyObservers(new ConnectionStateChangeEvent(this, false));
	}
	
	public void closeForce()
	{
		if(this.closeCheck("closeForce")) return;
		
		this.isRun = false;
		
		this.channels.clear();
		
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" 소켓 종료중 오류", e);
		}
		this.connectionStateChangeObservable.notifyObservers(new ConnectionStateChangeEvent(this, false));
	}
	
	public void setGeneralDataReceiveCallback(GeneralDataReceiveCallback callback)
	{
		this.generalDataReceiveCallback = callback;
	}
	
	public InetAddress getInetAddress()
	{	
		return this.socket.getInetAddress();
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
					logger.log(Level.WARNING, this.toString()+" 소켓 읽기중 오류1: " + readSize);
					this.closeForce();
					return;
				}
				
				if(ProtocolDefine.checkOption(buffer[0], ProtocolDefine.OPTION_SOCKET_CLOSE))
				{
					this.closeForce();
					return;
				}
				if(ProtocolDefine.checkOption(buffer[0], ProtocolDefine.OPTION_CHANNEL))
				{
					this.channelReceive(buffer[0]);
				}
				else
				{
					this.noneChannelReceive(buffer[0]);
				}
				
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, this.toString()+" 소켓 읽기중 오류3", e);
				this.closeForce();
				return;
			}
			
		}
	}
	
	private void channelReceive(byte option) throws IOException
	{
		ByteBuffer buf = ByteBuffer.wrap(ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.RANGE_CHANNEL));
		short id = buf.getShort();
		
		if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_PAYLOAD))
		{
			AppDataPacketAnalyser packetAnalyser = new AppDataPacketAnalyser(option, this.inputStream);
			if(this.channels.containsKey(id))
			{
				Channel channel = this.channels.get(id);
				channel.receiveTask(packetAnalyser);
			}
			else
			{
				logger.log(Level.WARNING, this.toString()+" 데이터가 왔는데 해당하는 채널이 없음");
				return;
			}
		}
		else if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_CHANNEL_OPEN))
		{
			Channel channel = new Channel(id, this.getKeyFromStream(), this.outputStream, this);
			this.channels.put(id, channel);
			this.notifyObservers(new ChannelEvent(channel, true));
			logger.log(Level.INFO, "채널 열기 " + this.getInetAddress().toString() + " " + id + " " + channel.key);
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
				logger.log(Level.WARNING, this.toString()+" 닫기를 시도하는 채널이 없음");
				this.closeForce();
				return;
			}
		}
	}
	
	private void noneChannelReceive(byte option) throws IOException
	{
		
		if(ProtocolDefine.checkOption(option, ProtocolDefine.OPTION_PAYLOAD))
		{
			String key = this.getKeyFromStream();
			
			AppDataPacketAnalyser packetAnalyser = new AppDataPacketAnalyser(option, this.inputStream);
			while(!packetAnalyser.readData());
			
			if(this.generalDataReceiveCallback != null)
				this.generalDataReceiveCallback.receiveData(this, key, packetAnalyser.payload);
		}
	}
	
	private String getKeyFromStream() throws IOException
	{
		ByteBuffer buf = ByteBuffer.wrap(ProtocolDefine.fillBuffer(this.inputStream, ProtocolDefine.RANGE_PAYLOAD_DATALEN));
		int keySize = buf.getInt();
		String key = new String(ProtocolDefine.fillBuffer(this.inputStream, keySize));
		return key;
	}
	
	public Channel channelOpen(String key)
	{
		if(this.closeCheck("channelOpen")) return null;
		
		short id = this.findEmptyChannel();
		this.assignID[id] = true;
		
		if(id == -1)
		{
			logger.log(Level.SEVERE, this.toString()+" 더이상 채널 ID를 할당할 수 없음");
			this.closeForce();
			return null;
		}
		Channel channel = new Channel(id, key, this.outputStream, this);
		if(channel.sendChannelOpenMsg(id, key))
		{
			this.channels.put(id, channel);
			this.notifyObservers(new ChannelEvent(channel, true));
			return channel;
		}
		return null;
	}
	
	public void sendData(String key, AppDataPacketBuilder builder)
	{
		if(this.closeCheck("sendData")) return;
		
		byte[][] allPayload = builder.getPayload();
		byte option = builder.writeOption((byte) 0);
		try
		{
			synchronized (this.outputStream)
			{
				this.outputStream.write(option);
				this.outputStream.write(ProtocolDefine.intToByteArray(key.length()));
				this.outputStream.write(key.getBytes());
				for(byte[] payload : allPayload)
				{
					this.outputStream.write(payload);
				}
			}
		}
		catch(IOException e)
		{
			logger.log(Level.WARNING, this.toString()+" 데이터 전송중 오류", e);
		}

	}
	
	public void sendData(String key, String value)
	{
		AppDataPacketBuilder builder = new AppDataPacketBuilder();
		builder.appendData(value);
		this.sendData(key, builder);
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
	
	public void closeAllChannel()
	{
		if(this.closeCheck("closeAllChannel")) return;
		
		for(Channel channel : this.channels.values())
		{
			this.assignID[channel.id] = false;
			channel.sendChannelCloseMessage();
		}
		this.channels.clear();
	}
	
	public void closeChannel(Channel channel)
	{
		if(this.closeCheck("closeChannel")) return;
		
		this.assignID[channel.id] = false;
		this.channels.remove(channel.id);

		channel.sendChannelCloseMessage();
	}
	
	private boolean closeCheck(String func)
	{
		if(!this.isRun)
		{
			throw new RuntimeException(this.toString()+"연결은 이미 닫힘 " + func);
		}
		return false;
	}
	
	public boolean isOpen()
	{
		return this.isRun;
	}
	
	@Override
	public String toString()
	{
		String str = "connection:"+this.getInetAddress().getHostAddress();
		return str;
	}
}