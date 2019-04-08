package ase.sensorManager;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.db.DB_Handler;
import ase.db.DB_Installer;
import ase.sensorManager.sensor.DataReceiveEvent;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensor.SensorConfigAccess;
import ase.sensorManager.sensor.SensorDBAccess;
import ase.sensorManager.sensor.SensorOnlineEvent;
import ase.sensorReader.DevicePacket;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorManager extends Observable<SensorRegisterEvent> implements Observer<DevicePacket> 
{
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");

	private Observable<DevicePacket> sensorReader;
	private DB_Handler dbHandler;
	
	private SensorDBAccess dbAccess;
	private SensorConfigAccess configAccess;
	
	private int checkInterval;
	
	private boolean isRun;
	private Thread timeoutCheckThread;
	private HashMap<Integer, Sensor> _sensorMap;
	public Map<Integer, Sensor> sensorMap;
	
	public final Observable<DataReceiveEvent> publicDataReceiveObservable;
	public final Observable<SensorOnlineEvent> publicSensorOnlineObservable;
	
	public SensorManager(DB_Handler dbHandler, Observable<DevicePacket> sensorReader)
	{
		this.sensorReader = sensorReader;
		this.dbHandler = dbHandler;
		this._sensorMap = new HashMap<Integer, Sensor>();
		this.sensorMap = Collections.unmodifiableMap(this._sensorMap);
		
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
			this._sensorMap.put(s.id, s);
		}
		
		this.checkTimeoutTask();
		
		this.checkInterval = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_CHECK_INTERVAL));
		
		this.sensorReader.addObserver(this);
		
		
		this.timeoutCheckThread = new Thread(this::timeoutCheck);
		this.timeoutCheckThread.setDaemon(true);
		this.timeoutCheckThread.start();
		
		logger.log(Level.INFO, "SensorManager 시작 완료");
		
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		logger.log(Level.INFO, "SensorManager관리자 종료");
		
		this.publicDataReceiveObservable.clearObservers();
		this.publicSensorOnlineObservable.clearObservers();
		
		if(this.timeoutCheckThread != null) this.timeoutCheckThread.interrupt();
		
		this.sensorReader.removeObserver(this);
		
		for(Sensor s : this._sensorMap.values())
		{
			logger.log(Level.INFO, s.id+"센서 정보 저장중...");
			s.save();
		}
		logger.log(Level.INFO, "SensorManager 관리자 종료 완료");
		this._sensorMap.clear();
	}

	@Override
	public void update(Observable<DevicePacket> object, DevicePacket data)
	{
		Sensor s = this._sensorMap.getOrDefault(data.ID, null);
		if(s == null)
		{// 새 장치 접근
			s = this.registerSensor(data.ID);
		}
		s.alartDataReceive(data.X_GRADIANT, data.Y_GRADIANT, data.X_ACCEL, data.Y_ACCEL, data.Z_ACCEL, data.Altitiude);
	}
	
	private void checkTimeoutTask()
	{
		Date compareTime = new Date();
		
		for(Sensor sensor : this._sensorMap.values())
		{
			sensor.checkDeviceTimeout(compareTime);
		}
	}
	
	public Sensor registerSensor(int id)
	{
		if(this._sensorMap.containsKey(id))
		{
			logger.log(Level.SEVERE, "이미 있는 장치"+id);
			return null;
		}
		Sensor s = new Sensor(id, this.dbAccess, this.configAccess, this.publicDataReceiveObservable, this.publicSensorOnlineObservable);
		s.init();
		this._sensorMap.put(id, s);
		logger.log(Level.INFO, "새 센서 접근:"+s.id);
		SensorRegisterEvent e = new SensorRegisterEvent(true, s);
		this.notifyObservers(e);
		return s;
	}
	
	public void removeSensor(int id)
	{
		Sensor s = this._sensorMap.getOrDefault(id, null);
		if(s == null)
		{
			logger.log(Level.SEVERE, "존재하지 않는 장치"+id);
			return;
		}
		s.destroy();
		this._sensorMap.remove(s.id);
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
