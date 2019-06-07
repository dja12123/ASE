package ase.o2SensorDataAnalyser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2Data;
import ase.util.observer.Observer;

public class O2SensorDataAnalyser
{
	private final Observer<O2DataReceiveEvent> o2DataObserver;
	private final Observer<SensorRegisterEvent> sensorRegisterObserver;
	private final SensorManager sensorManager;
	
	private Map<Sensor, SafetyStatus> safeMap;
	
	public O2SensorDataAnalyser(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this.o2DataObserver = this::o2DataObserver;
		this.sensorRegisterObserver = this::sensorRegisterObserver;
		this.safeMap = new HashMap<>();
	}
	
	public boolean startModule()
	{
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			this.safeMap.put(sensor, SafetyStatus.Safe);
		}
		this.sensorManager.registerObservable.addObserver(this.sensorRegisterObserver);
		this.sensorManager.dataO2Manager.addObserver(this.o2DataObserver);
		return true;
	}
	
	public void stopModule()
	{
		this.sensorManager.dataO2Manager.removeObserver(this.o2DataObserver);
		this.sensorManager.registerObservable.removeObserver(this.sensorRegisterObserver);

		this.safeMap.clear();
	}
	
	private void o2DataObserver(O2DataReceiveEvent e)
	{
	
	}
	
	private synchronized void sensorRegisterObserver(SensorRegisterEvent e)
	{
		if(e.isActive)
		{
			this.safeMap.put(e.sensor, SafetyStatus.Safe);
		}
		else
		{
			this.safeMap.remove(e.sensor);
		}
	}
}
