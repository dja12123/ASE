package ase.sensorManager.o2SensorDataAnalyser;

import java.util.LinkedList;
import java.util.Queue;

import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.sensorDataO2.SensorO2Data;

public class SensorDataAnalyser
{
	private final SensorConfigAccess configAccess;
	private final Queue<SensorO2Data> dataQueue;
	
	public SensorDataAnalyser(SensorConfigAccess configAccess)
	{
		this.configAccess = configAccess;
		this.dataQueue = new LinkedList<>();
	}
	
	public void pushData(SensorO2Data data)
	{
		this.dataQueue.add(data);
		SensorO2Data peekData = this.dataQueue.peek();
		if(peekData != null)
		{
			float def = data.value - peekData.value;
			
			while(data.time.getTime() - peekData.time.getTime() < this.configAccess.getAnalyseInterval())
			{
				this.dataQueue.poll();
				peekData = this.dataQueue.peek();
			}
		}



	}
	

}
