package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.AccelDataReceiveEvent;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.util.observer.Observer;

public class RealtimeO2DataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeO2ValueRequest";
	private final SensorManager sensorManager;
	private Sensor sensor;
	private Observer<O2DataReceiveEvent> sensorDataObserver;
	
	public RealtimeO2DataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		this.sensorManager.dataO2Manager.addObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.sensorManager.dataO2Manager.removeObserver(this.sensorDataObserver);
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

	private void sensorDataObserver(O2DataReceiveEvent event)
	{
		if(this.sensor == null) return;
		if(event.sensorInst.ID != this.sensor.ID) return;
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("time", DATE_FORMAT.format(event.data.time));
		json.addProperty("value", event.data.value);

		this.channel.sendData(json.toString());
	}
}
