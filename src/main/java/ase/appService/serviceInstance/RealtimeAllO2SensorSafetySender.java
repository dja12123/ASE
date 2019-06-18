package ase.appService.serviceInstance;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;

import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.o2SensorDataAnalyser.SafeStateChangeEvent;
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

	}
	
	private void safeObserver(SafeStateChangeEvent e)
	{
		
		this.channel.sendData(String.format("%d/%d", e.sensor.ID, e.status.code));
	}
	
}
