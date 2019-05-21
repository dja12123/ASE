package ase.sensorManager;

import ase.ServerCore;

public class SensorConfigAccess
{
	public static final String PROP_SENSOR_LOG_MAX = "SensorLogMax";
	public static final String PROP_SENSOR_DATA_CHACHE_MAX = "SensorCacheMax";
	public static final String PROP_SENSOR_TIMEOUT = "SensorTimeout";
	
	private int maxLog; 
	private int maxData;
	private int timeout;
	
	public SensorConfigAccess()
	{
		this.loadConfig();
	}
	
	public void loadConfig()
	{
		this.maxLog = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_LOG_MAX));
		this.maxData = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_DATA_CHACHE_MAX));
		this.timeout = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_TIMEOUT));
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
}
