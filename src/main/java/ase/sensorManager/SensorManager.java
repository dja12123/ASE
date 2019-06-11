package ase.sensorManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorManager.accelSensorDataAnalyser.AccelSensorDataAnalyser;
import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorControl.SensorControlInterface;
import ase.sensorManager.sensorDataAccel.SensorAccelDataManager;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.sensorManager.sensorOnline.SensorOnlineCheck;
import ase.util.observer.Observable;

public class SensorManager extends Observable<SensorRegisterEvent>
{
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	public static final String PROP_SENSOR_ID_LIST = "SensorIDList";
	
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");

	private final ISensorCommManager sensorComm;
	public final SensorLogManager sensorLogManager;
	public final SensorOnlineCheck sensorOnlineCheck;
	public final SensorAccelDataManager dataAccelManager;
	public final SensorO2DataManager dataO2Manager;
	public final SensorConfigAccess configAccess;
	public final O2SensorDataAnalyseManager dataAnalyseManager;
	public final SensorControlInterface sensorControl;
	public final AccelSensorDataAnalyser accelSensorDataAnalyser;
	
	private boolean isRun;
	private HashMap<Integer, Sensor> _sensorMap;
	public Map<Integer, Sensor> sensorMap;
	
	public SensorManager(ISensorCommManager sensorReader)
	{
		this.sensorComm = sensorReader;
		this.configAccess = new SensorConfigAccess();
		this.sensorLogManager = new SensorLogManager(this, this.configAccess);
		this.sensorOnlineCheck = new SensorOnlineCheck(this, this.sensorComm, this.sensorLogManager);
		this.dataAccelManager = new SensorAccelDataManager(this, this.sensorComm);
		this.dataO2Manager = new SensorO2DataManager(this, this.sensorComm, this.configAccess);
		this.dataAnalyseManager = new O2SensorDataAnalyseManager(this, this.dataO2Manager, this.sensorLogManager);
		this.sensorControl = new SensorControlInterface(this, this.sensorComm, this.sensorLogManager);
		this.accelSensorDataAnalyser = new AccelSensorDataAnalyser(this, this.configAccess, this.dataAccelManager, this.sensorLogManager);
		this._sensorMap = new HashMap<Integer, Sensor>();
		this.sensorMap = Collections.unmodifiableMap(this._sensorMap);
	}
	
	public synchronized boolean startModule()
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
		this.sensorLogManager.startModule();
		this.sensorOnlineCheck.startModule();
		this.dataO2Manager.startModule();
		this.dataAccelManager.startModule();
		this.dataAnalyseManager.startModule();
		this.sensorControl.startModule();
		this.accelSensorDataAnalyser.startModule();
		logger.log(Level.INFO, "SensorManager 시작 완료");
		
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.accelSensorDataAnalyser.stopModule();
		this.sensorControl.stopModule();
		this.dataAnalyseManager.stopModule();
		this.dataAccelManager.stopModule();
		this.dataO2Manager.stopModule();
		this.sensorOnlineCheck.stopModule();
		this.sensorLogManager.stopModule();
		Set<Integer> keySet = new HashSet<>();
		keySet.addAll(this._sensorMap.keySet());
		for(int key : keySet)
		{
			this.removeSensor(key);
		}
		this.clearObservers();
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
