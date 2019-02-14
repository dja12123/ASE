package telco.sensorReadServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.AppConnectObservable;
import telco.appConnect.AppDataReceiveEvent;
import telco.appConnect.ConnectionStateChangeEvent;
import telco.appConnect.protocol.AppDataPacketBuilder;
import telco.appConnect.protocol.Channel;
import telco.appConnect.protocol.Connection;
import telco.console.LogWriter;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class ServerSocketManager
{
	public static final Logger logger = LogWriter.createLogger(ServerSocketManager.class, "serverSocket");
	
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
		logger.log(Level.INFO, "ServerSocketManager 시작");
		
		this.port = Integer.parseInt(ServerCore.getProp(PROP_SERVERPORT));
		
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
		this.acceptThread.setDaemon(true);
		this.acceptThread.start();
		logger.log(Level.INFO, "서버 소켓 열기 "+this.socket.getInetAddress()+":"+this.port);
		
		
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
					AppDataPacketBuilder builder = new AppDataPacketBuilder();
					for(int i = 0; i < payload.length; ++i)
					{
						builder.appendData(payload[i]);
						System.out.print(new String(payload[i]) + "("+payload[i].length+ ") 다음데이타:");
					}
					System.out.println();
					builder.appendData("반사!!");
					ch.sendData(builder);
					System.out.println("반사완료");
				});
			}
			else
			{
				System.out.print("일반수신 " + data.payload.length + "개, 데이타:");
				for(int i = 0; i < data.payload.length; ++i)
				{
					System.out.print(new String(data.payload[i]) + "("+data.payload[i].length+ ") 다음데이타:");
				}
				
				System.out.println();
			}
		};
		eventProvider.addDataReceiveObserver("login", ob);
		eventProvider.addDataReceiveObserver("test1", ob);
		eventProvider.addDataReceiveObserver("onlyDataTest", ob);
		logger.log(Level.INFO, "ServerSocketManager 시작 완료");
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
		
		logger.log(Level.INFO, "ServerSocketManager 종료");
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