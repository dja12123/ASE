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
import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorControl.SensorSafetyControl;
import ase.sensorManager.sensorDataAccel.SensorAccelDataManager;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.sensorManager.sensorOnline.SensorOnlineCheck;
import ase.sensorReader.DevicePacket;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorManager extends Observable<SensorRegisterEvent>
{
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	public static final String PROP_SENSOR_ID_LIST = "SensorIDList";
	
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");

	private final ISensorCommManager sensorComm;
	public final SensorOnlineCheck sensorOnlineCheck;
	public final SensorAccelDataManager dataAccelManager;
	public final SensorO2DataManager dataO2Manager;
	public final SensorConfigAccess configAccess;
	public final O2SensorDataAnalyseManager dataAnalyseManager;
	public final SensorSafetyControl sensorSafetyControl;
	
	private boolean isRun;
	private HashMap<Integer, Sensor> _sensorMap;
	public Map<Integer, Sensor> sensorMap;
	
	public SensorManager(ISensorCommManager sensorReader)
	{
		this.sensorComm = sensorReader;
		this.sensorOnlineCheck = new SensorOnlineCheck(this, this.sensorComm);
		this.dataAccelManager = new SensorAccelDataManager(this, this.sensorComm);
		this.dataO2Manager = new SensorO2DataManager(this, this.sensorComm);
		this.configAccess = new SensorConfigAccess();
		this.dataAnalyseManager = new O2SensorDataAnalyseManager(this, this.dataO2Manager);
		this.sensorSafetyControl = new SensorSafetyControl(this, this.sensorComm);
		this._sensorMap = new HashMap<Integer, Sensor>();
		this.sensorMap = Collections.unmodifiableMap(this._sensorMap);
	}
	
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		logger.log(Level.INFO, "SensorManager 시작");

		this.configAccess.loadConfig();
				
		String sensorID = ServerCore.getProp(PROP_SENSOR_ID_LIST);
		
		for(String idstr : sensorID.split(","))
		{
			int id = Integer.parseInt(idstr.trim());
			this.registerSensor(id);
		}
		this.sensorOnlineCheck.startModule();
		this.dataO2Manager.startModule();
		this.dataAccelManager.startModule();
		this.dataAnalyseManager.startModule();
		this.sensorSafetyControl.startModule();
		logger.log(Level.INFO, "SensorManager 시작 완료");
		
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.sensorSafetyControl.stopModule();
		this.dataAccelManager.stopModule();
		this.dataO2Manager.stopModule();
		this.dataAccelManager.stopModule();
		this.sensorOnlineCheck.stopModule();
		this._sensorMap.clear();
	}


	public synchronized boolean registerSensor(int id)
	{
		if(this._sensorMap.containsKey(id)) return false;

		if(!this.sensorComm.getUserMap().containsKey(id))
		{
			if(!this.sensorComm.addUser(id))
			{
				logger.log(Level.WARNING, "잘못된 아이디의 센서 등록 시도 " + id);
				return false;
			}
		}
		else
		{
			return false;
		}
		Sensor sensor = new Sensor(id);
		this._sensorMap.put(id, sensor);
		SensorRegisterEvent e = new SensorRegisterEvent(true, sensor);
		this.notifyObservers(e);
		return true;
	}
	
	public synchronized boolean removeSensor(int id)
	{
		if(!this._sensorMap.containsKey(id)) return false;
		
		SensorRegisterEvent e = new SensorRegisterEvent(false, this._sensorMap.get(id));
		this._sensorMap.remove(id);
		this.notifyObservers(e);
		return true;
	}

}
