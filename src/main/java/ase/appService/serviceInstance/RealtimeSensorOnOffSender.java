package ase.appService.serviceInstance;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class RealtimeSensorOnOffSender extends ServiceInstance
{
	public static final String KEY = "RealtimeSensorOnOffRequest";
	private final SensorManager sensorManager;
	private Sensor sensor;
	private Observer<SensorOnlineEvent> sensorDataObserver;
	
	public RealtimeSensorOnOffSender(IChannel channel, SensorManager sensorManager)
	{
		super(KEY, channel);
		this.sensorManager = sensorManager;
		this.sensor = null;
		this.sensorDataObserver = this::sensorDataObserver;
	}

	@Override
	protected void onStartService()
	{
		
	}

	@Override
	protected void onDestroy()
	{
		if(this.sensor != null)
		{
			this.sensor.sensorOnlineObservable.removeObserver(this.sensorDataObserver);
		}
	}

	@Override
	protected void onDataRecive(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		String data = event.getStringPayload();
		try
		{
			Integer.valueOf(data);
		}
		catch (NumberFormatException e)
		{
			this.channel.sendData("result/"+false);
			this.destroy();
			return;
		}
		int id = Integer.parseInt(data);
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		if(sensor != null)
		{
			if(this.sensor != null)
			{
				this.sensor.sensorOnlineObservable.removeObserver(this.sensorDataObserver);
			}
			this.sensor = sensor;
			this.sensor.sensorOnlineObservable.addObserver(this.sensorDataObserver);
			this.channel.sendData("result/"+true);
			this.channel.sendData("state/"+this.sensor.isOnline());
		}
		else
		{
			this.channel.sendData("result/"+false);
			this.destroy();
		}
	}
	
	private void sensorDataObserver(Observable<SensorOnlineEvent> provider, SensorOnlineEvent event)
	{
		this.channel.sendData("state/"+event.isOnline);
	}

}
