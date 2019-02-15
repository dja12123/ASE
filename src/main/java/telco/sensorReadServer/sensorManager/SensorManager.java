package telco.sensorReadServer.sensorManager;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.console.LogWriter;
import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.db.DB_Handler;
import telco.sensorReadServer.db.DB_Installer;
import telco.sensorReadServer.sensorManager.sensor.DataReceiveEvent;
import telco.sensorReadServer.sensorManager.sensor.Sensor;
import telco.sensorReadServer.sensorManager.sensor.SensorConfigAccess;
import telco.sensorReadServer.sensorManager.sensor.SensorDBAccess;
import telco.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import telco.sensorReadServer.serialReader.DevicePacket;
import telco.sensorReadServer.serialReader.SerialReadManager;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class SensorManager extends Observable<SensorRegisterEvent> implements Observer<DevicePacket> 
{
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");

	private SerialReadManager serialReader;
	private DB_Handler dbHandler;
	
	private SensorDBAccess dbAccess;
	private SensorConfigAccess configAccess;
	
	private int checkInterval;
	
	private boolean isRun;
	private Thread timeoutCheckThread;
	private HashMap<Integer, Sensor> sensorMap;
	
	public final Observable<DataReceiveEvent> publicDataReceiveObservable;
	public final Observable<SensorOnlineEvent> publicSensorOnlineObservable;
	
	public SensorManager(SerialReadManager serialReader, DB_Handler dbHandler)
	{
		this.serialReader = serialReader;
		this.dbHandler = dbHandler;
		this.sensorMap = new HashMap<Integer, Sensor>();
		
		this.publicDataReceiveObservable = new Observable<DataReceiveEvent>();
		this.publicSensorOnlineObservable = new Observable<SensorOnlineEvent>();
	}
	
	public boolean startModule(DB_Installer dbinit)
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		logger.log(Level.INFO, "SensorManager 시작");

		this.dbAccess = new SensorDBAccess(dbHandler, dbinit);
		this.configAccess = new SensorConfigAccess();
		
		for(Sensor s : this.dbAccess.getSensorFromDB(this.configAccess, this.publicDataReceiveObservable, this.publicSensorOnlineObservable))
		{
			this.sensorMap.put(s.id, s);
		}
		
		this.checkTimeoutTask();
		
		this.checkInterval = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_CHECK_INTERVAL));
		this.serialReader.addObserver(this);
		
		this.timeoutCheckThread = new Thread(this::timeoutCheck);
		this.timeoutCheckThread.setDaemon(true);
		this.timeoutCheckThread.start();
		
		logger.log(Level.INFO, "SensorManager 시작 완료");
		
		this.publicDataReceiveObservable.addObserver((Observable<DataReceiveEvent> o, DataReceiveEvent e)->{
			System.out.println(e.data.toString());
		});
		
		this.publicSensorOnlineObservable.addObserver((Observable<SensorOnlineEvent> o, SensorOnlineEvent e)->{
			System.out.println(e.isOnline + " " + e.sensor.id);
		});
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		logger.log(Level.INFO, "SensorManager관리자 종료");
		
		this.publicDataReceiveObservable.clearObservers();
		this.publicSensorOnlineObservable.clearObservers();
		
		this.timeoutCheckThread.interrupt();
		
		this.serialReader.removeObserver(this);
		
		for(Sensor s : this.sensorMap.values())
		{
			s.save();
		}
		logger.log(Level.INFO, "SensorManager 관리자 종료 완료");
		this.sensorMap.clear();
	}

	@Override
	public void update(Observable<DevicePacket> object, DevicePacket data)
	{
		Sensor s = this.sensorMap.getOrDefault(data.ID, null);
		if(s == null)
		{// 새 장치 접근
			s = this.registerSensor(data.ID);
		}
		s.alartDataReceive(data.X_GRADIANT, data.Y_GRADIANT, data.X_ACCEL, data.Y_ACCEL, data.Z_ACCEL, data.Altitiude);
	}
	
	private void checkTimeoutTask()
	{
		Date compareTime = new Date();
		
		for(Sensor sensor : this.sensorMap.values())
		{
			sensor.CheckDeviceTimeout(compareTime);
		}
	}
	
	public Sensor registerSensor(int id)
	{
		if(this.sensorMap.containsKey(id))
		{
			throw new RuntimeException("이미 있는 장치");
		}
		Sensor s = new Sensor(id, this.dbAccess, this.configAccess, this.publicDataReceiveObservable, this.publicSensorOnlineObservable);
		s.init();
		this.sensorMap.put(id, s);
		logger.log(Level.INFO, "새 센서 접근:"+s.id);
		SensorRegisterEvent e = new SensorRegisterEvent(true, s);
		this.notifyObservers(e);
		return s;
	}
	
	public void removeSensor(int id)
	{
		Sensor s = this.sensorMap.getOrDefault(id, null);
		if(s == null)
		{
			throw new RuntimeException("존재하지 않는 장치");
		}
		s.destroy();
		this.sensorMap.remove(s.id);
		logger.log(Level.INFO, "센서 삭제:"+s.id);
		SensorRegisterEvent e = new SensorRegisterEvent(false, s);
		this.notifyObservers(e);
	}
	
	private void timeoutCheck()
	{
		while(this.isRun)
		{
			this.checkTimeoutTask();
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
