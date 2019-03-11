package ase.test;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.appConnect.Connection;
import ase.appConnect.ConnectionStateChangeEvent;
import ase.console.LogWriter;
import ase.util.observer.Observable;

public class ClientSocketManager extends Observable<ConnectionStateChangeEvent>
{
	public static final Logger logger = LogWriter.createLogger(ClientSocketManager.class, "clientSocket");
	
	public final String addr;
	public final int port;
	
	private Connection connection;
	
	
	public ClientSocketManager(String addr, int port)
	{
		this.addr = addr;
		this.port = port;
	}
	
	public void startConnection()
	{
		if(this.connection != null && this.connection.isOpen())
		{
			this.connection.closeSafe();
		}
		
		Socket socket;
		try
		{
			socket = new Socket(this.addr, this.port);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "연결 실패", e);
			return;
		}
		
		this.connection = new Connection(socket, this);
		this.connection.startConnection();
	}
	
	public void closeConnection()
	{
		if(this.connection != null && this.connection.isOpen())
		{
			this.connection.closeSafe();
		}
	}
	
	public Connection getConenction()
	{
		return this.connection;
	}
}
