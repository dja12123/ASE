package ase.appService.serviceInstance;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.alias.SensorAliasManager;
import ase.sensorManager.sensor.Sensor;

public class SensorOptionChange extends ServiceInstance
{
	public static final String KEY = "SensorSetting";
	
	private final SensorManager sensorManager;
	private final SensorAliasManager aliasManager;
	
	public SensorOptionChange(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.aliasManager = this.sensorManager.sensorAliasManager;
	}

	@Override
	protected void onStartService()
	{
		
	}

	@Override
	protected void onDestroy()
	{
		
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		String data = event.getStringPayload();
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(data).getAsJsonObject();
		if(json.get("getValue").getAsBoolean())
		{
			return;
		}
		switch(json.get("settingKey").getAsString())
		{
		case "sensorAlias":
			this.sensorAlias(json.get("settingValue"));
			break;
		
		}
	}
	
	private void sensorAlias(JsonElement jsonElement)
	{
		JsonObject valueObj = jsonElement.getAsJsonObject();
		int sensorID = valueObj.get("sensorID").getAsInt();
		String alias = valueObj.get("sensorAlias").getAsString();
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(sensorID, null);
		if(sensor == null)
		{
			return;
		}
		
		if(!this.aliasManager.setAlias(sensor, alias))
		{
			return;
		}
	}
	
}
