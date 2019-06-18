package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.AccelDataReceiveEvent;
import ase.sensorManager.sensorDataAccel.SensorAccelDataManager;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.util.observer.Observer;

public class RealtimeSensorDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorDataRequest";
	private final SensorManager sensorManager;
	private final SensorAccelDataManager sensorAccelDataManager;
	private Sensor sensor;
	private Observer<AccelDataReceiveEvent> sensorDataObserver;
	
	public RealtimeSensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensorAccelDataManager = this.sensorManager.dataAccelManager;
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		this.sensorAccelDataManager.addObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.sensorAccelDataManager.removeObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		String data = event.getStringPayload();
		JsonObject json = new JsonObject();
		
		try
		{
			Integer.valueOf(data);
		}
		catch (NumberFormatException e)
		{
			json.addProperty("result", false);
			this.channel.sendData(json.toString());
			this.destroy();
			return;
		}
		int id = Integer.parseInt(data);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		if(sensor != null)
		{
			this.sensor = sensor;
		
			json.addProperty("result", true);
		}
		else
		{
			json.addProperty("result", false);
		}
		this.channel.sendData(json.toString());
	}

	private void sensorDataObserver(AccelDataReceiveEvent event)
	{
		if(this.sensor == null) return;
		if(event.sensorInst.ID != this.sensor.ID) return;
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("time", DATE_FORMAT.format(event.data.time));
		json.addProperty("xa", event.data.X_ACCEL);
		json.addProperty("ya", event.data.Y_ACCEL);
		json.addProperty("za", event.data.Z_ACCEL);

		this.channel.sendData(json.toString());
	}
}
