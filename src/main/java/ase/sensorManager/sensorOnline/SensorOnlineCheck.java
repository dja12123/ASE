package ase.sensorManager.sensorOnline;

import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorComm.CommOnlineEvent;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ISensorTransmitter;
import ase.sensorManager.AbsSensorEventStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.util.observer.Observer;

public class SensorOnlineCheck extends AbsSensorEventStateManager<SensorOnlineEvent, Boolean>
{
	public static final Logger logger = LogWriter.createLogger(SensorOnlineCheck.class, "SensorOnlineCheck");
	
	private final ISensorCommManager commManager;
	private final SensorLogManager sensorLogManager;
	private final Observer<CommOnlineEvent> onlineObserver;
	
	public SensorOnlineCheck(SensorManager sensorManager, ISensorCommManager commManager, SensorLogManager sensorLogManager)
	{
		super(sensorManager);
		this.commManager = commManager;
		this.sensorLogManager = sensorLogManager;
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
			if(event.isOnline)
			{
				this.sensorLogManager.appendLog(sensor, Level.INFO, "Sensor Online");
				logger.log(Level.INFO, sensor.ID + " 센서 온라인");
			}
			else
			{
				this.sensorLogManager.appendLog(sensor, Level.INFO, "Sensor Offline");
				logger.log(Level.INFO, sensor.ID + " 센서 오프라인");
			}
			SensorOnlineEvent onlineEvent = new SensorOnlineEvent(sensor, event.isOnline);
			this.changeState(sensor, event.isOnline);
			this.provideEvent(sensor, onlineEvent);
		}
	}
	
}
