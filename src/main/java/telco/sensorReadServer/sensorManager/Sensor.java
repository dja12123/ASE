package telco.sensorReadServer.sensorManager;

import java.util.Date;

import telco.util.observer.Observable;

public class Sensor
{
	public final Observable<DataReceiveEvent> dataReceiveObservable;
	public final int id;
	
	boolean isOnline;
	private Date lastUpdateTime;
	
	public Sensor(int id)
	{
		this(id, new Date());
	}
	
	public Sensor(int id, Date lastUpdateTime)
	{
		this.id = id;
		this.dataReceiveObservable = new Observable<DataReceiveEvent>();
		this.lastUpdateTime = lastUpdateTime;
	}
	
	void alartDataReceive(float xg, float yg, float xa, float ya, float za, float al)
	{
	
		this.lastUpdateTime = new Date();
		DataReceiveEvent e = new DataReceiveEvent(this, xg, yg, xa, ya, za, al);
		this.dataReceiveObservable.notifyObservers(e);
	}
	
	public Date getLastUpdateTime()
	{
		return this.lastUpdateTime;
	}
	
	public boolean isOnline()
	{
		return this.isOnline;
	}
}
