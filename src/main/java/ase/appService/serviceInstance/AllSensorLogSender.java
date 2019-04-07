package ase.appService.serviceInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensor.SensorLog;
import ase.util.observer.Observable;

public class AllSensorLogSender extends ServiceInstance
{
	public static final String KEY = "AllSensorLogRequest";
	private final SensorManager sensorManager;
	
	public AllSensorLogSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
	}

	@Override
	protected void onStartService()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDataReceive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
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
			int sendStart = sensor.log.size() - size;
			if(sendStart < 0) sendStart = 0;
			for(int i = sendStart; i < sensor.log.size(); ++i)
			{
				SensorLog sensorLog = sensor.log.get(i);
				JsonObject data = new JsonObject();
				data.addProperty("time", DATE_FORMAT.format(sensorLog.time));
				data.addProperty("level", sensorLog.level.toString());
				data.addProperty("message", sensorLog.message);
				dataArray.add(data);
			}
			json.add("sensorData", dataArray);
		}
		else
		{
			json.addProperty("result", false);
		}
		this.channel.sendData(json.toString());
		this.destroy();
	}


}
