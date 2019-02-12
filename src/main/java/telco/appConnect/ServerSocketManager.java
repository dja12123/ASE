package telco.appConnect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.protocol.Channel;
import telco.appConnect.protocol.Connection;
import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.console.LogWriter;
import telco.sensorReadServer.util.observer.Observable;
import telco.sensorReadServer.util.observer.Observer;

public class ServerSocketManager
{
	public static final Logger logger = LogWriter.createLogger(ServerSocketManager.class, "appConnect");
	
	public static final String PROP_SERVERPORT = "Port";
	
	public final AppConnectObservable eventProvider;
	
	private boolean isRun;
	private int port;
	private ServerSocket socket;
	private Thread acceptThread;
	private ArrayList<Connection> clientList;
	
	public ServerSocketManager()
	{
		this.eventProvider = new AppConnectObservable();
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
		
		eventProvider.addConnectionStateChangeObserver((Observable<ConnectionStateChangeEvent> object, ConnectionStateChangeEvent data)->{
			if(data.isOpen)
			{
				System.out.println("연결생성" + data.connection.getInetAddress().toString());
			}
			else
			{
				System.out.println("연결종료" +  data.connection.getInetAddress().toString());
			}
		});
		Observer<AppDataReceiveEvent> ob = (Observable<AppDataReceiveEvent> object, AppDataReceiveEvent data)->{
			if(data.hasChannel)
			{
				System.out.println("채널생성" +  data.connection.getInetAddress().toString() + " " + data.key + " " + data.channel.id);
				data.channel.setReceiveCallback((Channel ch, byte[][] payload)->{
					System.out.print(ch.toString() + "으로부터 수신 " + payload.length + "개, 데이타:");
					for(int i = 0; i < payload.length; ++i)
					{
						System.out.print(new String(payload[i]) + " 다음데이타:");
					}
					System.out.println();
				});
			}
			else
			{
				System.out.println("데이타수신" +  data.connection.getInetAddress().toString() + " " + data.key +" "+ data.payload.length);
			}
		};
		eventProvider.addDataReceiveObserver("test", ob);
		eventProvider.addDataReceiveObserver("test1", ob);
		eventProvider.addDataReceiveObserver("onlyDataTest", ob);
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.eventProvider.clearObservers();
		
		for(Connection client : this.clientList)
		{
			client.closeSafe();
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
		Connection connection;
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
			connection = new Connection(clientSocket, this.eventProvider);
			this.clientList.add(connection);
			if(connection.startConnection())
			{
				logger.log(Level.INFO, "정상 연결");
			}
		}
	}
}