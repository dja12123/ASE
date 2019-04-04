package ase.sensorReadServer.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.sensorReadServer.sensorManager.sensor.SensorLog;
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
			if(this.sensor != null)
			{
				this.sensor.sensorLogObservable.removeObserver(this.sensorDataObserver);
			}
			this.sensor = sensor;
			this.sensor.sensorLogObservable.addObserver(this.sensorDataObserver);
			json.addProperty("result", true);
			json.addProperty("isOnline", this.sensor.isOnline());
		}
		else
		{
			json.addProperty("result", false);
		}
	}

}
