package ase.appServiceO2.serviceInstance;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.SensorAccelData;
import ase.sensorManager.sensorDataO2.SensorO2Data;

public class AllPreviouseO2DataSender extends ServiceInstance
{
	public static final String KEY = "AllPreviouseO2DataRequest";
	private final SensorManager sensorManager;

	public AllPreviouseO2DataSender(IChannel channel, SensorManager sensorManager)
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
		String input = event.getStringPayload();
		try
		{
			Integer.valueOf(input);
		}
		catch (Exception e)
		{
			json.addProperty("result", false);
			this.channel.sendData(json.toString());
			this.destroy();
			return;
		}
		int size = Integer.parseInt(input);
		json.addProperty("result", true);
		
		JsonArray sensorArray = new JsonArray();
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			JsonArray sensorDataArray = new JsonArray();
			
			List<SensorO2Data> dataList = this.sensorManager.dataO2Manager.getPreviouseSensorData(sensor);
			int sendStart = dataList.size() - size;
			if(sendStart < 0) sendStart = 0;
			for(int i = sendStart; i < dataList.size(); ++i)
			{
				SensorO2Data sensorData = dataList.get(i);
				JsonObject data = new JsonObject();
				data.addProperty("time", DATE_FORMAT.format(sensorData.time));
				data.addProperty("value", sensorData.value);
				sensorDataArray.add(data);
			}
			sensorArray.add(sensorDataArray);
		}
		json.add("sensors", sensorArray);
	}
}
