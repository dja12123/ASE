package ase.sensorReadServer.appService;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.appConnect.Connection;
import ase.appConnect.ConnectionStateChangeEvent;
import ase.console.LogWriter;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.serverSocket.ServerSocketManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class AppServiceManager implements Observer<ConnectionStateChangeEvent>
{
	public static final Logger logger = LogWriter.createLogger(AppServiceManager.class, "appServiceManager");
	
	private ServerSocketManager serverSocket;
	private SensorManager sensorManager;
	private HashMap<Connection, ServiceInst> serviceMap;
	
	public AppServiceManager(ServerSocketManager serverSocket, SensorManager sensorManager)
	{
		this.serverSocket = serverSocket;
		this.sensorManager = sensorManager;
		this.serviceMap = new HashMap<Connection, ServiceInst>();
	}
	
	public boolean startModule()
	{
		logger.log(Level.INFO, "앱 서비스 매니저 시작");
		this.serverSocket.addObserver(this);
		logger.log(Level.INFO, "앱 서비스 매니저 시작 완료");
		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "앱 서비스 매니저 종료 시작");
		this.serverSocket.removeObserver(this);
		for(ServiceInst inst : this.serviceMap.values())
		{
			inst.destroy();
		}
		this.serviceMap.clear();
		logger.log(Level.INFO, "앱 서비스 매니저 종료");
	}

	@Override
	public void update(Observable<ConnectionStateChangeEvent> object, ConnectionStateChangeEvent data)
	{
		
		if(data.isOpen)
		{
			logger.log(Level.INFO, "앱 서비스 인스턴스 생성");
			ServiceInst inst = new ServiceInst(data.connection, this.sensorManager);
			this.serviceMap.put(data.connection, inst);
		}
		else
		{
			if(this.serviceMap.containsKey(data.connection))
			{
				logger.log(Level.INFO, "앱 서비스 인스턴스 삭제");
				this.serviceMap.get(data.connection).destroy();
				this.serviceMap.remove(data.connection);
			}
		}
	}
}
