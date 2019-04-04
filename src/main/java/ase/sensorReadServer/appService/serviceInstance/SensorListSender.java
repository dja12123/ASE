package ase.sensorReadServer.appService.serviceInstance;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.Sensor;

public class SensorListSender extends ServiceInstance
{
	public static final String KEY = "SensorListRequest";

	private final SensorManager sensorManager;

	public SensorListSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
	}

	@Override
	protected void onDestroy()
	{

	}

	@Override
	protected void onStartService()
	{
		JsonObject json = new JsonObject();
		JsonArray dataSensorList = new JsonArray();

		List<Sensor> sensorList = new ArrayList<>(this.sensorManager.sensorMap.size());
		sensorList.addAll(this.sensorManager.sensorMap.values());
		json.addProperty("count", sensorList.size());
		json.add("data", dataSensorList);
		for (Sensor sensor : sensorList)
		{
			JsonObject data = new JsonObject();
			data.addProperty("id", sensor.id);
			data.addProperty("on", sensor.isOnline());
			dataSensorList.add(data);
		}
		this.channel.sendData(json.toString());
		this.destroy();
	}
}
