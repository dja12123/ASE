package telco.sensorReadServer.sensorManager;

import java.util.Date;

import telco.util.observer.Observable;

public class Sensor
{
	public final Observable<DataReceiveEvent> dataReceiveObservable;
	public final int id;
	
	boolean isOnline;
	Date lastDataReceiveTime;
	
	public Sensor(int id)
	{
		this.id = id;
		this.dataReceiveObservable = new Observable<DataReceiveEvent>();
		this.lastDataReceiveTime = null;
	}
	
	void alartDataReceive(int xg, int yg, int xa, int ya, int za, int al)
	{
		this.lastDataReceiveTime = new Date();
		DataReceiveEvent e = new DataReceiveEvent(this, xg, yg, xa, ya, za, al);
		this.dataReceiveObservable.notifyObservers(e);
	}
	
	public boolean isOnline()
	{
		return this.isOnline;
	}
}
