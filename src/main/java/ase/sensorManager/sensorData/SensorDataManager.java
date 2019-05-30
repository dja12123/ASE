package ase.sensorManager.sensorData;

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

public class SensorDataManager extends Observable<DataReceiveEvent>
{
	public static final Logger logger = LogWriter.createLogger(SensorDataManager.class, "SensorDataManager");
	
	private final SensorManager sensorManager;
	private final ISensorCommManager commManager;
	private final KeyObserver<Short, ReceiveEvent> sensorReadObserver;
	
	public SensorDataManager(SensorManager sensorManager, ISensorCommManager commManager)
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
		System.out.println("receive");
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor == null) return;
		ByteBuffer buf = ByteBuffer.wrap(event.payload);
		int xa = buf.getInt();
		int ya = buf.getInt();
		int za = buf.getInt();
		SensorData sensorData = new SensorData(new Date(), xa, ya, za);
		logger.log(Level.INFO, key + " sensor data receive" + sensorData.toString());
	}
}
