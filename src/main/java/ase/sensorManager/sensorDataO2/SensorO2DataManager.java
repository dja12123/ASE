package ase.sensorManager.sensorDataO2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.ReceiveEvent;
import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.KeyObserver;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorO2DataManager extends Observable<O2DataReceiveEvent>
{
	public static final Logger logger = LogWriter.createLogger(SensorO2DataManager.class, "SensorO2DataManager");
	
	private final SensorManager sensorManager;
	private final ISensorCommManager commManager;
	private final KeyObserver<Short, ReceiveEvent> sensorReadObserver;
	private final Observer<SensorRegisterEvent> sensorRegisterObserver;
	private final Map<Sensor, List<SensorO2Data>> _previousSensorData;
	private final Map<Sensor, List<SensorO2Data>> _umPreviousSensorData;

	public SensorO2DataManager(SensorManager sensorManager, ISensorCommManager commManager)
	{
		this.sensorManager = sensorManager;
		this.commManager = commManager;
		this.sensorReadObserver = this::sensorReadObserver;
		this.sensorRegisterObserver = this::sensorRegisterObserver;
		this._previousSensorData = new HashMap<>();
		this._umPreviousSensorData = new HashMap<>();
	}
	
	public synchronized void startModule()
	{
		this.commManager.addObserver(ProtoDef.KEY_C2S_O2SENSOR_DATA, this.sensorReadObserver);
		this.sensorManager.registerObservable.addObserver(this.sensorRegisterObserver);
	}
	
	public synchronized void stopModule()
	{
		this.commManager.removeObserver(ProtoDef.KEY_C2S_O2SENSOR_DATA, this.sensorReadObserver);
		this.sensorManager.registerObservable.removeObserver(this.sensorRegisterObserver);
		this._previousSensorData.clear();
		this._umPreviousSensorData.clear();
		this.clearObservers();
	}
	
	private void sensorReadObserver(short key, ReceiveEvent event)
	{
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor == null) return;
		ByteBuffer buf = ByteBuffer.wrap(event.payload);
		float value = buf.getFloat();
		SensorO2Data sensorData = new SensorO2Data(new Date(), value);
		List<SensorO2Data> dataList = this._previousSensorData.getOrDefault(sensor, null);
		if(dataList != null)
		{
			dataList.add(sensorData);
			int maxData = this.sensorManager.configAccess.getMaxData();
			while(dataList.size() > maxData)
			{
				dataList.remove(0);
			}
		}
		O2DataReceiveEvent dataReceiveEvent = new O2DataReceiveEvent(sensor, sensorData);
		this.notifyObservers(dataReceiveEvent);
		logger.log(Level.INFO, dataReceiveEvent.toString());
	}
	
	public List<SensorO2Data> getPreviouseSensorData(Sensor sensor)
	{
		List<SensorO2Data> dataList = this._umPreviousSensorData.get(sensor);
		return dataList;
	}
	
	private synchronized void sensorRegisterObserver(SensorRegisterEvent e)
	{
		if(e.isActive)
		{
			List<SensorO2Data> dataList = new ArrayList<>();
			this._previousSensorData.put(e.sensor, dataList);
			this._umPreviousSensorData.put(e.sensor, Collections.unmodifiableList(dataList));
		}
		else
		{
			this._previousSensorData.remove(e.sensor);
			this._umPreviousSensorData.remove(e.sensor);
		}
	}
}
