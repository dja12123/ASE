package telco.sensorReadServer.sensorManager;

import java.util.HashMap;
import java.util.logging.Logger;

import telco.console.LogWriter;
import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.db.DB_Handler;
import telco.sensorReadServer.db.DB_Installer;
import telco.sensorReadServer.serialReader.DevicePacket;
import telco.sensorReadServer.serialReader.SerialReadManager;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class SensorManager extends Observable<SensorStateChangeEvent> implements Observer<DevicePacket> 
{
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");
	public static final String PROP_SENSOR_TIMEOUT = "SensorTimeout";
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	public static final String DB_Schema = 
			"CREATE TABLE Device(" + 
			"id INTEGER," + 
			"lastUpdateTime TEXT," + 
			"PRIMARY KEY(`id`)" + 
			");";
	
	private SerialReadManager serialReader;
	private DB_Handler dbHandler;
	
	private boolean isRun;
	private Thread timeoutCheckThread;
	private HashMap<Integer, Sensor> sensorMap;
	private int sensorTimeout;
	private int checkInterval;
	
	public SensorManager(SerialReadManager serialReader, DB_Handler dbHandler)
	{
		this.serialReader = serialReader;
		this.dbHandler = dbHandler;
		this.sensorMap = new HashMap<Integer, Sensor>();
	}
	
	public boolean startModule(DB_Installer dbinit)
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		dbinit.checkAndCreateTable(DB_Schema);
		
		this.sensorTimeout = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_TIMEOUT));
		this.checkInterval = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_CHECK_INTERVAL));
		this.serialReader.addObserver(this);
		
		this.timeoutCheckThread = new Thread(this::timeoutCheck);
		this.timeoutCheckThread.setDaemon(true);
		this.timeoutCheckThread.start();
		
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.timeoutCheckThread.interrupt();
		
		this.serialReader.removeObserver(this);
		this.sensorMap.clear();
	}

	@Override
	public void update(Observable<DevicePacket> object, DevicePacket data)
	{
		
		
	}
	
	public void registerDevice(int id)
	{
		
	}
	
	private void timeoutCheck()
	{
		while(this.isRun)
		{
			
			
			try
			{
				Thread.sleep(this.checkInterval);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
