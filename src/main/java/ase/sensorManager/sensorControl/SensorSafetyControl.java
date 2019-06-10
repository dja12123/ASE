package ase.sensorManager.sensorControl;

import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.protocolSerial.SerialProtoDef;
import ase.sensorComm.protocolSerial.SerialTransmitter;
import ase.sensorManager.SensorManager;
import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.o2SensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.o2SensorDataAnalyser.SafetyStatus;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.Observer;

public class SensorSafetyControl
{
	private final SensorManager sensorManager;
	private final O2SensorDataAnalyseManager dataAnalyseManager;
	private final ISensorCommManager sensorComm;
	private final Observer<SafeStateChangeEvent> safeStateChangeObserver;
	
	public SensorSafetyControl(SensorManager sensorManager, ISensorCommManager sensorComm)
	{
		this.sensorManager = sensorManager;
		this.dataAnalyseManager = sensorManager.dataAnalyseManager;
		this.sensorComm = sensorComm;
		this.safeStateChangeObserver = this::safeStateChangeObserver;
	}
	
	public synchronized void startModule()
	{
		for(Sensor sensor : this.dataAnalyseManager.state.keySet())
		{
			SafetyStatus stat = this.dataAnalyseManager.state.get(sensor);
			if(stat != null)
			{
				this.sendData(sensor, stat);
			}
		}
		this.dataAnalyseManager.addObserver(this.safeStateChangeObserver);
	}
	
	public synchronized void stopModule()
	{
		this.dataAnalyseManager.removeObserver(this.safeStateChangeObserver);
	}
	
	public void safeStateChangeObserver(SafeStateChangeEvent event)
	{
		this.sendData(event.sensor, event.status);
	}
	
	private void sendData(Sensor sensor, SafetyStatus stat)
	{
		SerialTransmitter transmitter = this.sensorComm.getUserMap().getOrDefault(sensor.ID, null);
		if(transmitter != null)
		{
			transmitter.putSegment(ProtoDef.KEY_S2C_SAFETY_STAT, stat.code);
		}
	}
	
}
