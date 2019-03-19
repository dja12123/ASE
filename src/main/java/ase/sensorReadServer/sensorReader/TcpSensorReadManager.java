package ase.sensorReadServer.sensorReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorReadServer.ServerCore;
import ase.util.observer.Observable;

public class TcpSensorReadManager extends Observable<DevicePacket>
{
	public static final Logger logger = LogWriter.createLogger(TcpSensorReadManager.class, "tcpSensorReader");
	
	public static final String PROP_SERVERPORT = "TCPSensorPort";
	
	private boolean isRun;
	private int port;
	private ServerSocket socket;
	private Thread acceptThread;
	private ArrayList<TcpSensorConnect> clientList;
	
	public TcpSensorReadManager()
	{
		this.isRun = false;
		this.clientList = new ArrayList<>();
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
		logger.log(Level.INFO, "TcpSensorReadManager 시작 완료");
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.clearObservers();
		
		ArrayList<TcpSensorConnect> closeList = new ArrayList<>();
		closeList.addAll(this.clientList);
		
		for(TcpSensorConnect client : closeList)
		{
			client.close();
		}
		
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
		TcpSensorConnect connection;
		while(this.isRun)
		{
			try
			{
				clientSocket = this.socket.accept();
				logger.log(Level.INFO, "센서 소켓 accpet 성공");
			}
			catch (IOException e)
			{
				continue;
			}
			connection = new TcpSensorConnect(clientSocket, this);
			this.clientList.add(connection);
			
			logger.log(Level.INFO, "센서 정상 연결");
		}
	}

	public void socketCloseCallback(TcpSensorConnect tcpSensorConnect)
	{
		this.clientList.remove(tcpSensorConnect);
		
	}
}
