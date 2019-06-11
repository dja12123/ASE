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

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.ReceiveEvent;
import ase.sensorManager.AbsCommSensorStateManager;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.KeyObserver;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorO2DataManager extends AbsCommSensorStateManager<O2DataReceiveEvent, List<SensorO2Data>>
{
	public static final Logger logger = LogWriter.createLogger(SensorO2DataManager.class, "SensorO2DataManager");
	
	private final SensorConfigAccess config;
	private final Map<Sensor, List<SensorO2Data>> _previousSensorData;

	public SensorO2DataManager(SensorManager sensorManager, ISensorCommManager commManager, SensorConfigAccess config)
	{
		super(sensorManager, commManager, ProtoDef.KEY_C2S_O2SENSOR_DATA);
		this.config = config;
		this._previousSensorData = new HashMap<>();
	}
	
	public List<SensorO2Data> getPreviouseSensorData(Sensor sensor)
	{
		List<SensorO2Data> dataList = this.state.get(sensor);
		return dataList;
	}
	
	public SensorO2Data getLastSensorData(Sensor sensor)
	{
		List<SensorO2Data> dataList = this.state.get(sensor);
		if(!dataList.isEmpty())
		{
			return dataList.get(dataList.size() - 1);
		}
		return null;
	}

	@Override
	protected void onStart()
	{
		logger.log(Level.INFO, "산소 센서 리더 활성화");
	}

	@Override
	protected void onStop()
	{
		logger.log(Level.INFO, "산소 센서 리더 비활성화");
	}

	@Override
	protected List<SensorO2Data> onRegisterSensor(Sensor sensor)
	{
		List<SensorO2Data> dataList = new ArrayList<>();
		this._previousSensorData.put(sensor, dataList);
		return Collections.unmodifiableList(dataList);
	}

	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		this._previousSensorData.remove(sensor);
	}

	@Override
	protected void onReceive(Sensor sensor, byte[] payload)
	{
		List<SensorO2Data> dataList = this._previousSensorData.getOrDefault(sensor, null);
		if(dataList == null) return;
		ByteBuffer buf = ByteBuffer.wrap(payload);
		float value = buf.getFloat();
		SensorO2Data sensorData = new SensorO2Data(new Date(), value);
		dataList.add(sensorData);
		int maxData = this.config.getMaxData();
		while(dataList.size() > maxData)
		{
			dataList.remove(0);
		}
		
		O2DataReceiveEvent dataReceiveEvent = new O2DataReceiveEvent(sensor, sensorData);
		this.provideEvent(ServerCore.mainThreadPool, sensor, dataReceiveEvent);
	}
}
