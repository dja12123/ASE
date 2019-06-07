package ase.sensorManager;

import ase.ServerCore;

public class SensorConfigAccess
{
	public static final String PROP_SENSOR_LOG_MAX = "SensorLogMax";
	public static final String PROP_SENSOR_DATA_CHACHE_MAX = "SensorCacheMax";
	public static final String PROP_SENSOR_TIMEOUT = "SensorTimeout";
	public static final String PROP_SENSOR_ANALYSE_INTERVAL = "SensorAnalyseInterval";
	
	private int maxLog; 
	private int maxData;
	private int timeout;
	private int analyseInterval;
	
	public SensorConfigAccess()
	{
	}
	
	public void loadConfig()
	{
		this.maxLog = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_LOG_MAX));
		this.maxData = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_DATA_CHACHE_MAX));
		this.timeout = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_TIMEOUT));
		this.analyseInterval = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_ANALYSE_INTERVAL));
	}
	
	public int getMaxLog()
	{
		return this.maxLog;
	}
	
	public int getMaxData()
	{
		return this.maxData;
	}
	
	public int getTimeout()
	{
		return this.timeout;
	}
	
	public int getAnalyseInterval()
	{
		return this.analyseInterval;
	}
}
