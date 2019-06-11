package ase.appService.serviceInstance;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.SensorAccelData;
import ase.sensorManager.sensorDataO2.SensorO2Data;

public class PreviouseO2DataSender extends ServiceInstance
{
	public static final String KEY = "PreviouseO2DataRequest";
	private final SensorManager sensorManager;

	public PreviouseO2DataSender(IChannel channel, SensorManager sensorManager)
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

		if(sensor != null)
		{
			json.addProperty("result", true);
			JsonArray dataArray = new JsonArray();
			int size = Integer.parseInt(input[1]);
			List<SensorO2Data> dataList = this.sensorManager.dataO2Manager.getPreviouseSensorData(sensor);
			int sendStart = dataList.size() - size;
			if(sendStart < 0) sendStart = 0;
			for(int i = sendStart; i < dataList.size(); ++i)
			{
				SensorO2Data sensorData = dataList.get(i);
				JsonObject data = new JsonObject();
				data.addProperty("time", DATE_FORMAT.format(sensorData.time));
				data.addProperty("value", sensorData.value);
				dataArray.add(data);
			}
			json.add("sensorData", dataArray);
		}
		else
		{
			json.addProperty("result", false);
		}
		this.channel.sendData(json.toString());
	}
}
