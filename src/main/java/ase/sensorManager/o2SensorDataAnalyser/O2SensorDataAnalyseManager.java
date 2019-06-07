package ase.sensorManager.o2SensorDataAnalyser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ase.ServerCore;
import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2Data;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class O2SensorDataAnalyseManager
{
	public static final float SAFE_THRESHOLD = 0.20F;
	public static final float WARNING_THRESHOLD = 0.18F;
	
	public final Observable<SafeStateChangeEvent> safeStateChangeObservable;
	private final Observer<O2DataReceiveEvent> o2DataObserver;
	private final Observer<SensorRegisterEvent> sensorRegisterObserver;
	private final SensorManager sensorManager;
	
	private Map<Sensor, SafetyStatus> _safeMap;
	public Map<Sensor, SafetyStatus> safeMap;
	
	public O2SensorDataAnalyseManager(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this.safeStateChangeObservable = new Observable<SafeStateChangeEvent>();
		this.safeMap = Collections.synchronizedMap(this._safeMap);
		this.o2DataObserver = this::o2DataObserver;
		this.sensorRegisterObserver = this::sensorRegisterObserver;
		this._safeMap = new HashMap<>();
	}
	
	public void startModule()
	{
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			SensorO2Data data = this.sensorManager.dataO2Manager.getLastSensorData(sensor);
			if(data != null)
			{
				this._safeMap.put(sensor, this.checkSafe(data));
			}
			else
			{
				this._safeMap.put(sensor, SafetyStatus.Safe);
			}
		}
		this.sensorManager.registerObservable.addObserver(this.sensorRegisterObserver);
		this.sensorManager.dataO2Manager.addObserver(this.o2DataObserver);
	}
	
	public void stopModule()
	{
		this.sensorManager.dataO2Manager.removeObserver(this.o2DataObserver);
		this.sensorManager.registerObservable.removeObserver(this.sensorRegisterObserver);

		this._safeMap.clear();
	}
	
	private void o2DataObserver(O2DataReceiveEvent e)
	{
		SafetyStatus beforeStatus = this._safeMap.getOrDefault(e.sensorInst, null);
		if(beforeStatus != null)
		{
			SafetyStatus nowStatus = this.checkSafe(e.data);
			if(beforeStatus != nowStatus)
			{
				SafeStateChangeEvent event = new SafeStateChangeEvent(e.sensorInst, nowStatus);
				this._safeMap.put(e.sensorInst, nowStatus);
				this.safeStateChangeObservable.notifyObservers(ServerCore.mainThreadPool, event);
			}
		}
	}
	
	private SafetyStatus checkSafe(SensorO2Data data)
	{
		if(data.value >= SAFE_THRESHOLD)
		{//안전
			return SafetyStatus.Safe;
		}
		else if(data.value >= WARNING_THRESHOLD)
		{//주의
			return SafetyStatus.Warning;
		}
		else
		{//위험
			return SafetyStatus.Danger;
		}
	}
	
	private synchronized void sensorRegisterObserver(SensorRegisterEvent e)
	{
		if(e.isActive)
		{
			this._safeMap.put(e.sensor, SafetyStatus.Safe);
		}
		else
		{
			this._safeMap.remove(e.sensor);
		}
	}
}
