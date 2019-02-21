package telco.sensorReadServer.sensorManager.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import telco.sensorReadServer.sensorManager.SensorManager;
import telco.util.observer.Observable;

public class Sensor
{	
	public final Observable<DataReceiveEvent> dataReceiveObservable;
	public final Observable<SensorOnlineEvent> sensorOnlineObservable;
	public final Observable<SensorLog> sensorLogObservable;
	
	private Observable<DataReceiveEvent> publicDataReceiveObservable;
	private Observable<SensorOnlineEvent> publicSensorOnlineObservable;
	
	final List<SensorLog> _log;
	public final List<SensorLog> log;
	final List<SensorData> _data;
	public final List<SensorData> data;
	public final int id;
	private SensorDBAccess dbAccess;
	private SensorConfigAccess configAccess;
	private boolean isOnline;
	private Date lastUpdateTime;
	
	public Sensor(int id,
			SensorDBAccess dbAccess,
			SensorConfigAccess configAccess,
			Observable<DataReceiveEvent> publicDataReceiveObservable,
			Observable<SensorOnlineEvent> publicSensorOnlineObservable)
	{
		this(id, false, new Date(0), dbAccess, configAccess, publicDataReceiveObservable, publicSensorOnlineObservable);
	}
	
	Sensor(int id,
			boolean isOnline,
			Date lastUpdateTime,
			SensorDBAccess dbAccess,
			SensorConfigAccess configAccess,
			Observable<DataReceiveEvent> publicDataReceiveObservable,
			Observable<SensorOnlineEvent> publicSensorOnlineObservable)
	{
		this.id = id;
		this.isOnline = isOnline;
		this.lastUpdateTime = lastUpdateTime;
		this.dbAccess = dbAccess;
		this.configAccess = configAccess;
		
		this.dataReceiveObservable = new Observable<DataReceiveEvent>();
		this.sensorOnlineObservable = new Observable<SensorOnlineEvent>();
		this.sensorLogObservable = new Observable<SensorLog>();
		
		this.publicDataReceiveObservable = publicDataReceiveObservable;
		this.publicSensorOnlineObservable = publicSensorOnlineObservable;
		this._log = new ArrayList<SensorLog>();
		this.log = Collections.unmodifiableList(this._log);
		this._data = new ArrayList<SensorData>();
		this.data = Collections.unmodifiableList(this._data);
	}
	
	public void init()
	{
		this.dbAccess.createSensor(this);
	}
	
	public void destroy()
	{
		this.dbAccess.destroySensor(this);
		this.dataReceiveObservable.clearObservers();
		this.sensorOnlineObservable.clearObservers();
		this.sensorLogObservable.clearObservers();
	}
	
	public void save()
	{
		this.dbAccess.saveSensorState(this);
	}
	
	public void putLog(Level level, String msg)
	{
		SensorLog log = new SensorLog(level, new Date(), msg);
		this._log.add(log);
		if(this._log.size() > this.configAccess.getMaxLog())
		{
			this._log.remove(0);
		}
		this.sensorLogObservable.notifyObservers(log);
	}
	
	public void alartDataReceive(float xg, float yg, float xa, float ya, float za, float al)
	{
		if(!this.isOnline)
		{
			this.putLog(Level.INFO, "센서 온라인");
			SensorManager.logger.log(Level.INFO, this.id+"센서 온라인");
			this.isOnline = true;
			SensorOnlineEvent e = new SensorOnlineEvent(this, true);
			this.sensorOnlineObservable.notifyObservers(e);
			this.publicSensorOnlineObservable.notifyObservers(e);
		}
		
		SensorData data = new SensorData(new Date(), xg, yg, xa, ya, za ,al);
		this._data.add(data);
		if(this._data.size() > this.configAccess.getMaxData())
		{
			this._data.remove(0);
		}
		
		this.lastUpdateTime = new Date();
		DataReceiveEvent e = new DataReceiveEvent(this, data);
		this.dataReceiveObservable.notifyObservers(e);
		this.publicDataReceiveObservable.notifyObservers(e);
	}
	
	public void checkDeviceTimeout(Date nowTime)
	{
		if(this.isOnline)
		{
			long compareTime = nowTime.getTime();
			if(compareTime - this.lastUpdateTime.getTime() > this.configAccess.getTimeout())
			{//타임아웃일때
				this.putLog(Level.WARNING, "센서 타임아웃");
				SensorManager.logger.log(Level.WARNING, this.id+"센서 타임아웃");
				this.isOnline = false;
				SensorOnlineEvent e = new SensorOnlineEvent(this, false);
				this.sensorOnlineObservable.notifyObservers(e);
				this.publicSensorOnlineObservable.notifyObservers(e);
			}
		}
	}

	public Date getLastUpdateTime()
	{
		return this.lastUpdateTime;
	}
	
	public boolean isOnline()
	{
		return this.isOnline;
	}
	
	@Override
	public String toString()
	{
		return "sensor " + this.id;
	}
}
