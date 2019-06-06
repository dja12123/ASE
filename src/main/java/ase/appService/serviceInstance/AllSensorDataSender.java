package ase.appService.serviceInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.SensorAccelData;

public class AllSensorDataSender extends ServiceInstance
{
	public static final String KEY = "AllSensorDataRequest";
	private final SensorManager sensorManager;

	public AllSensorDataSender(IChannel channel, SensorManager sensorManager)
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

	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		JsonObject json = new JsonObject();
		String[] input;
		try
		{
			input = event.getStringPayload().split("/");
			Integer.valueOf(input[0]);
			Integer.valueOf(input[1]);
		}
		catch (Exception e)
		{
			json.addProperty("result", false);
			this.channel.sendData(json.toString());
			this.destroy();
			return;
		}
		int id = Integer.parseInt(input[0]);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);

		/*
		if(sensor != null)
		{
			json.addProperty("result", true);
			JsonArray dataArray = new JsonArray();
			int size = Integer.parseInt(input[1]);
			int sendStart = sensor.data.size() - size;
			if(sendStart < 0) sendStart = 0;
			for(int i = sendStart; i < sensor.data.size(); ++i)
			{
				SensorData sensorData = sensor.data.get(i);
				JsonObject data = new JsonObject();
				data.addProperty("time", DATE_FORMAT.format(sensorData.time));
				data.addProperty("xg", sensorData.X_GRADIANT);
				data.addProperty("yg", sensorData.Y_GRADIANT);
				data.addProperty("xa", sensorData.X_ACCEL);
				data.addProperty("ya", sensorData.Y_ACCEL);
				data.addProperty("za", sensorData.Z_ACCEL);
				data.addProperty("al", sensorData.Altitiude);
				dataArray.add(data);
			}
			json.add("sensorData", dataArray);
		}
		else
		{
			json.addProperty("result", false);
		}*/
		this.channel.sendData(json.toString());
		this.destroy();
	}
}
