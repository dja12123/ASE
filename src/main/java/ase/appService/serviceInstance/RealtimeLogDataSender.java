package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorLog.SensorLog;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.util.observer.Observer;

public class RealtimeLogDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeLogDataRequest";
	private final SensorManager sensorManager;
	private final SensorLogManager logManager;
	private Sensor sensor;
	private Observer<SensorLog> sensorDataObserver;
	
	public RealtimeLogDataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.logManager = this.sensorManager.sensorLogManager;
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		this.logManager.addObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDestroy()
	{
		if(this.sensor != null)
		{
			this.logManager.removeObserver(this.sensorDataObserver);
		}
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
	
	private void sensorDataObserver(SensorLog event)
	{
		if(event.sensor != this.sensor) return;
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("time", DATE_FORMAT.format(event.time));
		json.addProperty("level", event.level.toString());
		json.addProperty("message", event.message);
		this.channel.sendData(json.toString());
	}

}
