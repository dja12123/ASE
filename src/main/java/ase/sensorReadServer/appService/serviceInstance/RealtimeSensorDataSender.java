package ase.sensorReadServer.appService.serviceInstance;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.DataReceiveEvent;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeSensorDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorDataRequest";
	private final SensorManager sensorManager;
	private JsonParser parser;
	private Sensor sensor;
	private Observer<DataReceiveEvent> sensorDataObserver;
	
	public RealtimeSensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.parser = new JsonParser();
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		
	}

	@Override
	protected void onDestroy()
	{
		if(this.sensor != null)
		{
			this.sensor.dataReceiveObservable.removeObserver(this.sensorDataObserver);
		}
	}

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		String data = event.getStringPayload();
		try
		{
			Integer.valueOf(data);
		}
		catch (NumberFormatException e)
		{
			return;
		}
		int id = Integer.parseInt(data);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		JsonObject json = new JsonObject();
		if(sensor != null)
		{
			json.addProperty("result", true);
			json.addProperty("isOnline", this.sensor.isOnline());
			if(this.sensor != null)
			{
				this.sensor.dataReceiveObservable.removeObserver(this.sensorDataObserver);
			}
			this.sensor.dataReceiveObservable.addObserver(this.sensorDataObserver);
		}
		else
		{
			json.addProperty("result", false);
		}
	}

	private void sensorDataObserver(Observable<DataReceiveEvent> provider, DataReceiveEvent event)
	{
		JsonObject json = new JsonObject();
		json.addProperty("xg", event.data.X_GRADIANT);
		json.addProperty("yg", event.data.Y_GRADIANT);
		json.addProperty("xa", event.data.X_ACCEL);
		json.addProperty("ya", event.data.Y_ACCEL);
		json.addProperty("za", event.data.Z_ACCEL);
		json.addProperty("al", event.data.Altitiude);
		this.channel.sendData(json.toString());
	}
}
