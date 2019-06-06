package ase.sensorManager.sensorDataO2;

import java.util.Date;

public class SensorO2Data
{
	public final Date time;
	public final float value;
	
	public SensorO2Data(Date time, float value)
	{
		this.time = time;
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("TIME: "); buffer.append(this.time.toString()); buffer.append('\n');
		buffer.append("value: "); buffer.append(this.value); buffer.append('\n');
		
		return buffer.toString();
	}
}
