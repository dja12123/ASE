package ase.sensorManager.sensorControl;

public class SensorControlEvent
{
	public final ControlType type;
	public final byte[] rawData;
	
	public SensorControlEvent(ControlType type, byte[] rawData)
	{
		this.type = type;
		this.rawData = rawData;
	}
}
