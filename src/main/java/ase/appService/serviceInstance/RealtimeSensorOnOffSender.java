package ase.appService.serviceInstance;

import ase.appService.ServiceInstance;
import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorOnline.SensorOnlineCheck;
import ase.sensorManager.sensorOnline.SensorOnlineEvent;
import ase.util.observer.Observer;

public class RealtimeSensorOnOffSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorOnOffRequest";
	private final SensorManager sensorManager;
	private final SensorOnlineCheck onlineCheck;
	private Sensor sensor;
	private Observer<SensorOnlineEvent> sensorDataObserver;
	
	public RealtimeSensorOnOffSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.onlineCheck = sensorManager.sensorOnlineCheck;
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		this.onlineCheck.addObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDestroy()
	{
		this.onlineCheck.removeObserver(this.sensorDataObserver);
	}

	@Override
	protected void onDataReceive(ChannelDataEvent event)
	{
		String data = event.getStringPayload();
		try
		{
			Integer.valueOf(data);
		}
		catch (NumberFormatException e)
		{
			this.channel.sendData("result/"+false);
			return;
		}
		int id = Integer.parseInt(data);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		if(sensor != null)
		{
			this.sensor = sensor;
			this.channel.sendData("result/"+true);
			this.channel.sendData("state/"+this.onlineCheck.state.get(this.sensor));
		}
		else
		{
			this.channel.sendData("result/"+false);
		}
	}
	
	private void sensorDataObserver(SensorOnlineEvent event)
	{
		if(event.sensor != this.sensor) return;
		
		this.channel.sendData("state/"+event.isOnline);
		
	}

}
