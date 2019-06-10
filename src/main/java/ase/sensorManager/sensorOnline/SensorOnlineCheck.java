package ase.sensorManager.sensorOnline;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ase.sensorComm.CommOnlineEvent;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ISensorTransmitter;
import ase.sensorManager.AbsCommSensorStateManager;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.Observer;

public class SensorOnlineCheck extends AbsSensorStateManager<SensorOnlineEvent, Boolean>
{
	private final ISensorCommManager commManager;
	private final Observer<CommOnlineEvent> onlineObserver;
	
	public SensorOnlineCheck(SensorManager sensorManager, ISensorCommManager commManager)
	{
		super(sensorManager);
		this.commManager = commManager;
		this.onlineObserver = this::onlineObserver;
	}
	@Override
	protected void onStart()
	{
		this.commManager.addOnlineObserver(this.onlineObserver);
		
	}
	@Override
	protected void onStop()
	{
		this.commManager.removeOnlineObserver(this.onlineObserver);		
	}
	@Override
	protected Boolean onRegisterSensor(Sensor sensor)
	{
		ISensorTransmitter transmitter = this.commManager.getUserMap().getOrDefault(sensor.ID, null);
		if(transmitter != null)
		{
			return transmitter.isOnline();
		}
		return false;
	}
	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		
	}

	private void onlineObserver(CommOnlineEvent event)
	{
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor != null)
		{
			SensorOnlineEvent onlineEvent = new SensorOnlineEvent(sensor, event.isOnline);
			this.provideEvent(sensor, onlineEvent);
		}
	}
	
}
