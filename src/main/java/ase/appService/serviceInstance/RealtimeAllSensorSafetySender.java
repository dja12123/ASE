package ase.appService.serviceInstance;

import com.google.gson.JsonObject;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.accelSensorDataAnalyser.AccelSensorDataAnalyser;
import ase.sensorManager.accelSensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeAllSensorSafetySender extends ServiceInstance
{
	public static final String KEY = "RealtimeAllSensorSafetyRequest";
	
	private final SensorManager sensorManager;
	private final AccelSensorDataAnalyser dataAnalyser;
	private final Observer<SafeStateChangeEvent> observer;
	
	public RealtimeAllSensorSafetySender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.dataAnalyser = this.sensorManager.accelSensorDataAnalyser;
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

	}
	
	private void safeObserver(SafeStateChangeEvent e)
	{
		
		this.channel.sendData(String.format("%d/%d", e.sensor.ID, e.status.code));
	}
	
}
