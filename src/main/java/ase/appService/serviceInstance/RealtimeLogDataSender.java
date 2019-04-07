package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensor.SensorLog;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeLogDataSender extends ServiceInstance
{
	public static final String KEY = "RealtimeLogDataRequest";
	private final SensorManager sensorManager;
	private Sensor sensor;
	private Observer<SensorLog> sensorDataObserver;
	
	public RealtimeLogDataSender(IChannel channel, SensorManager sensorManager)
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
			this.sensor.sensorLogObservable.removeObserver(this.sensorDataObserver);
		}
	}

	@Override
	protected void onDataReceive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
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
				this.sensor.sensorLogObservable.removeObserver(this.sensorDataObserver);
			}
			this.sensor = sensor;
			this.sensor.sensorLogObservable.addObserver(this.sensorDataObserver);
			json.addProperty("result", true);
		}
		else
		{
			json.addProperty("result", false);
			this.destroy();
		}
		this.channel.sendData(json.toString());
	}
	
	private void sensorDataObserver(Observable<SensorLog> provider, SensorLog event)
	{
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("time", DATE_FORMAT.format(event.time));
		json.addProperty("level", event.level.toString());
		json.addProperty("message", event.message);
		this.channel.sendData(json.toString());
	}

}
