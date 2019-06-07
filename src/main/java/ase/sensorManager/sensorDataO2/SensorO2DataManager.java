package ase.sensorManager.sensorDataO2;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.ReceiveEvent;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.KeyObserver;
import ase.util.observer.Observable;

public class SensorO2DataManager extends Observable<O2DataReceiveEvent>
{
	public static final Logger logger = LogWriter.createLogger(SensorO2DataManager.class, "SensorO2DataManager");
	
	private final SensorManager sensorManager;
	private final ISensorCommManager commManager;
	private final KeyObserver<Short, ReceiveEvent> sensorReadObserver;
	
	public SensorO2DataManager(SensorManager sensorManager, ISensorCommManager commManager)
	{
		this.sensorManager = sensorManager;
		this.commManager = commManager;
		this.sensorReadObserver = this::sensorReadObserver;
	}
	
	public synchronized void startModule()
	{
		this.commManager.addObserver(ProtoDef.KEY_C2S_O2SENSOR_DATA, this.sensorReadObserver);
	}
	
	public synchronized void stopModule()
	{
		this.commManager.removeObserver(ProtoDef.KEY_C2S_O2SENSOR_DATA, this.sensorReadObserver);
		this.clearObservers();
	}
	
	private void sensorReadObserver(Short key, ReceiveEvent event)
	{
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor == null) return;
		ByteBuffer buf = ByteBuffer.wrap(event.payload);
		float value = buf.getFloat();
		SensorO2Data sensorData = new SensorO2Data(new Date(), value);
		O2DataReceiveEvent dataReceiveEvent = new O2DataReceiveEvent(sensor, sensorData);
		this.notifyObservers(dataReceiveEvent);
		logger.log(Level.INFO, dataReceiveEvent.toString());
	}
}