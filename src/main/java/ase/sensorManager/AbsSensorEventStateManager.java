package ase.sensorManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import ase.ServerCore;
import ase.sensorManager.sensor.Sensor;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public abstract class AbsSensorEventStateManager<Event, State> extends Observable<Event>
{
	protected final SensorManager sensorManager;
	private final Map<Sensor, State> _state;
	public final Map<Sensor, State> state;
	private final Observer<SensorRegisterEvent> sensorRegisterObserver;
	
	public AbsSensorEventStateManager(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this._state = new HashMap<>();
		this.state = Collections.unmodifiableMap(this._state);
		this.sensorRegisterObserver = this::sensorRegisterObserver;
	}
	
	public synchronized void startModule()
	{
		this.sensorManager.addObserver(this.sensorRegisterObserver);
		this.onStart();
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			State state = this.onRegisterSensor(sensor);
			this._state.put(sensor, state);
		}
	}
	
	abstract protected void onStart();
	
	public synchronized void stopModule()
	{
		this.sensorManager.removeObserver(this.sensorRegisterObserver);
		this.onStop();
		this._state.clear();
	}
	
	abstract protected void onStop();
	
	private synchronized void sensorRegisterObserver(SensorRegisterEvent event)
	{
		if(event.isActive)
		{
			this.state.put(event.sensor, this.onRegisterSensor(event.sensor));
			this.onRegisterSensor(event.sensor);
		}
		else
		{
			if(this._state.containsKey(event.sensor))
			{
				this.onRemoveSensor(event.sensor);
			}
		}
	}
	
	abstract protected State onRegisterSensor(Sensor sensor);
	
	abstract protected void onRemoveSensor(Sensor sensor);
	
	protected final void changeState(Sensor sensor, State state)
	{
		if(this.state.containsKey(sensor))
		{
			this._state.put(sensor, state);
		}
	}
	
	protected final void provideEvent(Sensor sensor, Event state)
	{
		this.notifyObservers(state);
	}
	
	protected final void provideEvent(ExecutorService pool, Sensor sensor, Event state)
	{
		this.notifyObservers(pool, state);
	}
}
