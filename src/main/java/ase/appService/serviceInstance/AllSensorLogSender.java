package ase.appService.serviceInstance;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorLog.SensorLog;
import ase.sensorManager.sensorLog.SensorLogManager;

public class AllSensorLogSender extends ServiceInstance
{
	public static final String KEY = "AllSensorLogRequest";
	private final SensorManager sensorManager;
	private final SensorLogManager logManager;

	public AllSensorLogSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.logManager = this.sensorManager.sensorLogManager;
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
			return;
		}
		int id = Integer.parseInt(input[0]);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);

		if(sensor != null)
		{
			json.addProperty("result", true);
			JsonArray dataArray = new JsonArray();
			int size = Integer.parseInt(input[1]);
			List<SensorLog> dataList = this.logManager.getPreviouseSensorLog(sensor);
			int sendStart = dataList.size() - size;
			if(sendStart < 0) sendStart = 0;
			for(int i = sendStart; i < dataList.size(); ++i)
			{
				SensorLog sensorLog = dataList.get(i);
				JsonObject data = new JsonObject();
				data.addProperty("time", DATE_FORMAT.format(sensorLog.time));
				data.addProperty("level", sensorLog.level.toString());
				data.addProperty("message", sensorLog.message);
				dataArray.add(data);
			}
			json.add("sensorLog", dataArray);
		}
		else
		{
			json.addProperty("result", false);
		}
		this.channel.sendData(json.toString());
	}
}
