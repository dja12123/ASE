package ase.appService.serviceInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorOnline.SensorOnlineCheck;

public class SensorListSender extends ServiceInstance
{
	public static final String KEY = "SensorListRequest";

	private final SensorManager sensorManager;
	private final SensorOnlineCheck onlineCheck;

	public SensorListSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.onlineCheck = sensorManager.sensorOnlineCheck;
	}

	@Override
	protected void onDestroy()
	{
		
	}

	@Override
	protected void onStartService()
	{

	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		if(!event.getStringPayload().equals("getdata"))
		{
			return;
		}
		JsonObject json = new JsonObject();
		JsonArray dataSensorList = new JsonArray();

		json.add("data", dataSensorList);
		for (Sensor sensor : this.sensorManager.sensorMap.values())
		{
			JsonObject data = new JsonObject();
			data.addProperty("id", sensor.ID);
			data.addProperty("on", this.onlineCheck.state.get(sensor));
			dataSensorList.add(data);
		}

		this.channel.sendData(json.toString());
		this.channel.sendData(json.toString());
	}
}
