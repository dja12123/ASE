package ase.sensorManager;

import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ReceiveEvent;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.KeyObserver;

public abstract class AbsCommSensorStateManager<Event, State> extends AbsSensorStateManager<Event, State>
{
	private final short commKey;
	protected final ISensorCommManager commManager;
	private final KeyObserver<Short, ReceiveEvent> sensorReadObserver;
	
	public AbsCommSensorStateManager(SensorManager sensorManager, ISensorCommManager commManager, short commKey)
	{
		super(sensorManager);
		this.commManager = commManager;
		this.sensorReadObserver = this::sensorReadObserver;
		this.commKey = commKey;
	}
	
	@Override
	public synchronized void startModule()
	{
		this.commManager.addObserver(this.commKey, this.sensorReadObserver);
		super.startModule();
	}
	
	@Override
	public synchronized void stopModule()
	{
		this.commManager.removeObserver(this.commKey, this.sensorReadObserver);
		super.stopModule();
	}
	
	private void sensorReadObserver(short key, ReceiveEvent event)
	{
		Sensor sensor = this.sensorManager.sensorMap.getOrDefault(event.ID, null);
		if(sensor != null)
		{
			this.onReceive(sensor, event.payload);
		}
	}
	
	protected abstract void onReceive(Sensor sensor, byte[] payload);

}
