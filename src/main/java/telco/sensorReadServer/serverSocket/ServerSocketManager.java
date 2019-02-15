package telco.sensorReadServer.serverSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.Connection;
import telco.appConnect.ConnectionStateChangeEvent;
import telco.console.LogWriter;
import telco.sensorReadServer.ServerCore;
import telco.util.observer.Observable;

public class ServerSocketManager extends Observable<ConnectionStateChangeEvent>
{
	public static final Logger logger = LogWriter.createLogger(ServerSocketManager.class, "serverSocket");
	
	public static final String PROP_SERVERPORT = "Port";
	
	private boolean isRun;
	private int port;
	private ServerSocket socket;
	private Thread acceptThread;
	private ArrayList<Connection> clientList;
	
	public ServerSocketManager()
	{
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
		
		logger.log(Level.INFO, "서버 소켓 열기 "+this.socket.getInetAddress());
		logger.log(Level.INFO, "ServerSocketManager 시작 완료");
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.clearObservers();
		
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
			connection = new Connection(clientSocket, this);
			this.clientList.add(connection);
			if(connection.startConnection())
			{
				logger.log(Level.INFO, "정상 연결");
			}
			this.notifyObservers(new ConnectionStateChangeEvent(connection, true));
		}
	}
}