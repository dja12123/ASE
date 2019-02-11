package telco.sensorReadServer.appConnect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.console.LogWriter;

public class AppConnectManager implements ConnectionUser
{
	public static final Logger logger = LogWriter.createLogger(AppConnectManager.class, "appConnect");
	
	public static final String PROP_SERVERPORT = "Port";
	
	private boolean isRun;
	private int port;
	private ServerSocket socket;
	private Thread acceptThread;
	private ArrayList<Connection> clientList;
	
	public AppConnectManager()
	{
		this.isRun = false;
		this.clientList = new ArrayList<Connection>();
	}
	
	public synchronized boolean startModule()
	{
		if(this.isRun) return false;
		this.isRun = true;
		
		this.port = Integer.parseInt(ServerCore.getProp(PROP_SERVERPORT));
		logger.log(Level.INFO, "서버 소켓 열기 " + this.port);
		try
		{
			this.socket = new ServerSocket(this.port);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "서버 소켓 오류", e);
			return false;
		}
		
		this.acceptThread = new Thread(this::socketAcceptThread);
		this.acceptThread.start();
		
		logger.log(Level.INFO, "AppConnectManager 시작");
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		for(Connection client : this.clientList)
		{
			client.closeConnection();
		}
		
		this.clientList.clear();
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "서버 소켓 종료중 오류", e);
		}
		
		logger.log(Level.INFO, "AppConnectManager 종료");
	}
	
	public void socketAcceptThread()
	{
		Socket clientSocket;
		Connection client;
		while(this.isRun)
		{
			try
			{
				clientSocket = this.socket.accept();
				logger.log(Level.INFO, "소켓 accpet 성공");
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "소켓 accpet 오류", e);
				continue;
			}
			client = new Connection(clientSocket, this);
			this.clientList.add(client);
			if(client.startConnection())
			{
				logger.log(Level.INFO, "정상 연결");
			}
		}
	}

	@Override
	public void createChannel(Connection connection, Channel channel)
	{
		logger.log(Level.INFO, "채널 생성 " + channel.id + " " + channel.key);
		channel.setReceiveCallback((Channel ch, byte[][] data)->{
			System.out.println("receive: " + ch.id + " " + ch.key);
			
			AppDataPacketBuilder b = ch.getPacketBuilder();
			for(int i = 0; i < data.length; ++i)
			{
				System.out.println(new String(data[i]));
				try
				{
					b.appendData(new String(data[i]));
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			ch.sendData(b);
			
		});
	}

	@Override
	public void closeConnection(Connection connection)
	{
		logger.log(Level.INFO, "연결 삭제");
		this.clientList.remove(connection);
	}
}