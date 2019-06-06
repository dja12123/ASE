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
	public static final Logger logger = LogWriter.createLogger(SensorO2DataManager.class, "SensorDataManager");
	
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
		this.commManager.addObserver(ProtoDef.KEY_C2S_SENSOR_DATA, this.sensorReadObserver);
	}
	
	public synchronized void stopModule()
	{
		this.commManager.removeObserver(ProtoDef.KEY_C2S_SENSOR_DATA, this.sensorReadObserver);
		this.clearObservers();
	}
	
	private void sensorReadObserver(Short key, ReceiveEvent event)
	{
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor == null) return;
		ByteBuffer buf = ByteBuffer.wrap(event.payload);
		int xa = buf.getInt();
		int ya = buf.getInt();
		int za = buf.getInt();
		SensorO2Data sensorData = new SensorO2Data(new Date(), xa, ya, za);
		O2DataReceiveEvent dataReceiveEvent = new O2DataReceiveEvent(sensor, sensorData);
		this.notifyObservers(dataReceiveEvent);
		logger.log(Level.INFO, dataReceiveEvent.toString());
	}
}
