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
import ase.sensorComm.ISensorCommManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.SensorAccelDataManager;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.sensorReader.DevicePacket;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorManager
{
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	public static final String PROP_SENSOR_ID_LIST = "SensorIDList";
	
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");

	private final ISensorCommManager sensorComm;
	public final SensorAccelDataManager dataAccelManager;
	public final SensorO2DataManager dataO2Manager;
	private SensorConfigAccess configAccess;
	
	private boolean isRun;
	private HashMap<Integer, Sensor> _sensorMap;
	public Map<Integer, Sensor> sensorMap;
	
	public final Observable<SensorRegisterEvent> registerObservable;
	
	public SensorManager(ISensorCommManager sensorReader)
	{
		this.sensorComm = sensorReader;
		this.dataAccelManager = new SensorAccelDataManager(this, this.sensorComm);
		this.dataO2Manager = new SensorO2DataManager(this, this.sensorComm);
		this._sensorMap = new HashMap<Integer, Sensor>();
		this.sensorMap = Collections.unmodifiableMap(this._sensorMap);
		this.registerObservable = new Observable<>();
	}
	
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		logger.log(Level.INFO, "SensorManager 시작");

		this.configAccess = new SensorConfigAccess();
		
		this.dataO2Manager.startModule();
		this.dataAccelManager.startModule();
		
		String sensorID = ServerCore.getProp(PROP_SENSOR_ID_LIST);
		
		for(String idstr : sensorID.split(","))
		{
			int id = Integer.parseInt(idstr.trim());
			this.registerSensor(id);
		}
		
		logger.log(Level.INFO, "SensorManager 시작 완료");
		
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.dataO2Manager.stopModule();
		this.dataAccelManager.stopModule();
		this._sensorMap.clear();
	}


	public synchronized boolean registerSensor(int id)
	{
		if(this._sensorMap.containsKey(id)) return false;
		Sensor sensor = new Sensor(id);
		this._sensorMap.put(id, sensor);
		if(!this.sensorComm.getUserMap().containsKey(id))
		{
			if(!this.sensorComm.addUser(id))
			{
				logger.log(Level.WARNING, "잘못된 아이디의 센서 등록 시도 " + id);
			}
		}
		
		return true;
	}
	
	public synchronized void removeSensor(int id)
	{
		
	}

}
