package ase.sensorManager.sensorData;

import ase.util.observer.Observable;

public class SensorDataManager
{
	public final Observable<DataReceiveEvent> sensorDataObservable;
	
	public SensorDataManager()
	{
		this.sensorDataObservable = new Observable<>();
	}
}
