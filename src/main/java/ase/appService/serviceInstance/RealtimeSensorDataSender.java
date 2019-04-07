package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.DataReceiveEvent;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeSensorDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorDataRequest";
	private final SensorManager sensorManager;
	private Sensor sensor;
	private Observer<DataReceiveEvent> sensorDataObserver;
	
	public RealtimeSensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
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
			if(this.sensor != null)
			{
				this.sensor.dataReceiveObservable.removeObserver(this.sensorDataObserver);
			}
			this.sensor = sensor;
			this.sensor.dataReceiveObservable.addObserver(this.sensorDataObserver);
			json.addProperty("result", true);
		}
		else
		{
			json.addProperty("result", false);
			this.destroy();
		}
		this.channel.sendData(json.toString());
	}

	private void sensorDataObserver(Observable<DataReceiveEvent> provider, DataReceiveEvent event)
	{
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("time", DATE_FORMAT.format(event.data.time));
		json.addProperty("xg", event.data.X_GRADIANT);
		json.addProperty("yg", event.data.Y_GRADIANT);
		json.addProperty("xa", event.data.X_ACCEL);
		json.addProperty("ya", event.data.Y_ACCEL);
		json.addProperty("za", event.data.Z_ACCEL);
		json.addProperty("al", event.data.Altitiude);
		this.channel.sendData(json.toString());
	}
}
