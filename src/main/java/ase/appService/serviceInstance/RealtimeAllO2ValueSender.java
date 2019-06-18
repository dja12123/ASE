package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeAllO2ValueSender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllO2ValueRequest";
	
	private final SensorManager sensorManager;
	private final Observer<O2DataReceiveEvent> observer;
	
	public RealtimeAllO2ValueSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.observer = this::o2Observer;
	}

	@Override
	protected void onStartService()
	{
		this.sensorManager.dataO2Manager.addObserver(this.observer);
		
	}

	@Override
	protected void onDestroy()
	{
		this.sensorManager.dataO2Manager.removeObserver(this.observer);
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{

	}
	
	private void o2Observer(O2DataReceiveEvent e)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", e.sensorInst.ID);
		json.addProperty("time", DATE_FORMAT.format(e.data.time));
		json.addProperty("value", e.data.value);
		this.channel.sendData(json.toString());
	}
	
}
