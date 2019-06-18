package ase.sensorManager.alias;

import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;

public class SensorAliasManager extends AbsSensorStateManager<String>
{
	public static final Logger logger = LogWriter.createLogger(SensorAliasManager.class, "SensorAliasManager");

	public SensorAliasManager(SensorManager sensorManager)
	{
		super(sensorManager);
	}

	@Override
	protected void onStart()
	{
		
	}

	@Override
	protected void onStop()
	{
		
	}

	@Override
	protected String onRegisterSensor(Sensor sensor)
	{
		return String.format("S%d", sensor.ID);
	}

	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		
	}
	
	public boolean setAlias(Sensor sensor, String alias)
	{
		if(!this.state.containsKey(sensor))
		{
			return false;
		}
		if(!(alias.charAt(0) >= 'A' && alias.charAt(0) <= 'Z'))
		{
			return false;
		}
		String numPart = alias.substring(1, alias.length());
		if(!numPart.isEmpty())
		{
			if(numPart.length() > 4)
			{
				return false;
			}
			try
			{
				Integer.parseInt(numPart);
			}
			catch(Exception e)
			{
				return false;
			}
		}

		this.changeState(sensor, alias);
		logger.log(Level.INFO, sensor.ID + " 센서 별명 수정: " + alias);
		return true;
	}

}
