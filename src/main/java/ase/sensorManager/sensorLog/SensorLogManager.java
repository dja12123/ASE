package ase.sensorManager.sensorLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;

public class SensorLogManager extends AbsSensorStateManager<SensorLog, List<SensorLog>>
{
	public static final Logger logger = LogWriter.createLogger(SensorLogManager.class, "SensorLogManager");
	
	private final SensorConfigAccess config;
	private final Map<Sensor, List<SensorLog>> _sensorLog;

	public SensorLogManager(SensorManager sensorManager, SensorConfigAccess config)
	{
		super(sensorManager);
		this.config = config;
		this._sensorLog = new HashMap<>();
	}
	
	public List<SensorLog> getPreviouseSensorLog(Sensor sensor)
	{
		List<SensorLog> dataList = this.state.get(sensor);
		return dataList;
	}
	
	public SensorLog getLastSensorData(Sensor sensor)
	{
		List<SensorLog> dataList = this.state.get(sensor);
		if(!dataList.isEmpty())
		{
			return dataList.get(dataList.size() - 1);
		}
		return null;
	}

	@Override
	protected void onStart()
	{
		logger.log(Level.INFO, "로그 메니저 활성화");
	}

	@Override
	protected void onStop()
	{
		logger.log(Level.INFO, "로그 메니저 비활성화");
	}

	@Override
	protected List<SensorLog> onRegisterSensor(Sensor sensor)
	{
		List<SensorLog> dataList = new ArrayList<>();
		this._sensorLog.put(sensor, dataList);
		return Collections.unmodifiableList(dataList);
	}

	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		this._sensorLog.remove(sensor);
	}
	
	public void appendLog(Sensor sensor, Level level, String msg)
	{
		List<SensorLog> logList = this._sensorLog.getOrDefault(sensor, null);
		if(logList == null) return;
		SensorLog sensorLog = new SensorLog(sensor, level, new Date(), msg);
		logList.add(sensorLog);
		int maxData = this.config.getMaxLog();
		while(logList.size() > maxData)
		{
			logList.remove(0);
		}
		this.provideEvent(ServerCore.mainThreadPool, sensor, sensorLog);
	}

}