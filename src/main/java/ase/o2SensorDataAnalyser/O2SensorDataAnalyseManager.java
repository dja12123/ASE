package ase.o2SensorDataAnalyser;

import java.util.HashMap;
import java.util.Map;

import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.util.observer.Observer;

public class O2SensorDataAnalyseManager
{
	public static final float SAFE_THRESHOLD = 0.20F;
	public static final float WARNING_THRESHOLD = 0.18F;
	
	private final Observer<O2DataReceiveEvent> o2DataObserver;
	private final Observer<SensorRegisterEvent> sensorRegisterObserver;
	private final SensorManager sensorManager;
	
	private Map<Sensor, SafetyStatus> safeMap;
	
	public O2SensorDataAnalyseManager(SensorManager sensorManager)
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
	
	private SafetyStatus checkSafe(float o2Percent)
	{
		if(o2Percent >= SAFE_THRESHOLD)
		{//안전
			
		}
		else if(o2Percent >= WARNING_THRESHOLD)
		{//주의
			
		}
		else
		{//위험
			
		}
		return null;
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
