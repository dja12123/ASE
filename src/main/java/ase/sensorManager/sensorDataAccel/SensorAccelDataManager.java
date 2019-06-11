package ase.sensorManager.sensorDataAccel;

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
import ase.sensorManager.AbsCommSensorStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;

public class SensorAccelDataManager extends AbsCommSensorStateManager<AccelDataReceiveEvent, List<SensorAccelData>>
{
	public static final Logger logger = LogWriter.createLogger(SensorAccelDataManager.class, "SensorAccelDataManager");

	private final Map<Sensor, List<SensorAccelData>> _previousSensorData;

	public SensorAccelDataManager(SensorManager sensorManager, ISensorCommManager commManager)
	{
		super(sensorManager, commManager, ProtoDef.KEY_C2S_ACCELSENSOR_DATA);
		this._previousSensorData = new HashMap<>();
	}
	
	public List<SensorAccelData> getPreviouseSensorData(Sensor sensor)
	{
		List<SensorAccelData> dataList = this.state.get(sensor);
		return dataList;
	}
	
	public SensorAccelData getLastSensorData(Sensor sensor)
	{
		List<SensorAccelData> dataList = this.state.get(sensor);
		if(!dataList.isEmpty())
		{
			return dataList.get(dataList.size() - 1);
		}
		return null;
	}

	@Override
	protected void onStart()
	{
		logger.log(Level.INFO, "가속도 센서 리더 활성화");
	}

	@Override
	protected void onStop()
	{
		logger.log(Level.INFO, "가속도 센서 리더 비활성화");
	}

	@Override
	protected List<SensorAccelData> onRegisterSensor(Sensor sensor)
	{
		List<SensorAccelData> dataList = new ArrayList<>();
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
		List<SensorAccelData> dataList = this._previousSensorData.getOrDefault(sensor, null);
		if(dataList == null) return;
		ByteBuffer buf = ByteBuffer.wrap(payload);
		int xa = buf.getInt();
		int ya = buf.getInt();
		int za = buf.getInt();
		SensorAccelData sensorData = new SensorAccelData(new Date(), xa, ya, za);
		dataList.add(sensorData);
		int maxData = this.sensorManager.configAccess.getMaxData();
		while(dataList.size() > maxData)
		{
			dataList.remove(0);
		}
		AccelDataReceiveEvent dataReceiveEvent = new AccelDataReceiveEvent(sensor, sensorData);
		this.provideEvent(ServerCore.mainThreadPool, sensor, dataReceiveEvent);
		logger.log(Level.INFO, dataReceiveEvent.toString());
	}
}
