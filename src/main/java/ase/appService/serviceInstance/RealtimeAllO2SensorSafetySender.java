package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;

import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.o2SensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.o2SensorDataAnalyser.SafetyStatus;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.Observer;

public class RealtimeAllO2SensorSafetySender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllO2SensorSafetyRequest";
	
	private final SensorManager sensorManager;
	private final O2SensorDataAnalyseManager dataAnalyser;
	private final Observer<SafeStateChangeEvent> observer;
	
	public RealtimeAllO2SensorSafetySender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.dataAnalyser = this.sensorManager.o2SensorDataAnalyser;
		this.observer = this::safeObserver;
	}

	@Override
	protected void onStartService()
	{
		this.dataAnalyser.addObserver(this.observer);
		
	}

	@Override
	protected void onDestroy()
	{
		this.dataAnalyser.removeObserver(this.observer);
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		if(!event.getStringPayload().equals("getData"))
		{
			return;
		}
		for(Sensor sensor : this.dataAnalyser.state.keySet())
		{
			SafetyStatus status = this.dataAnalyser.state.get(sensor);
			this.channel.sendData(String.format("%d/%d", sensor.ID, status.code));
		}
	}
	
	private void safeObserver(SafeStateChangeEvent e)
	{
		this.channel.sendData(String.format("%d/%d", e.sensor.ID, e.status.code));
	}
	
}
