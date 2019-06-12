package ase.sensorManager.accelSensorDataAnalyser;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataAccel.SensorAccelData;
import ase.util.SortedLinkedList;

public class SensorDataAnalyser
{
	public static final Logger logger = LogWriter.createLogger(SensorDataAnalyser.class, "SensorDataAnalyser");
	
	public static final int Threshold = 15;
	
	private final Sensor sensor;
	private final AccelSensorDataAnalyser manager;
	private final SensorConfigAccess configAccess;
	private final Queue<SensorAccelData> dataQueue;
	
	private SortedLinkedList<SensorAccelData> xSortedList;
	private SortedLinkedList<SensorAccelData> ySortedList;
	private SortedLinkedList<SensorAccelData> zSortedList;
	
	private SafetyStatus safetyStatus;

	public SensorDataAnalyser(Sensor sensor, AccelSensorDataAnalyser manager, SensorConfigAccess configAccess)
	{
		this.sensor = sensor;
		this.manager = manager;
		this.configAccess = configAccess;
		this.dataQueue = new LinkedList<>();

		this.xSortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.X_ACCEL - o2.X_ACCEL;});
		this.ySortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.Y_ACCEL - o2.Y_ACCEL;});
		this.zSortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.Z_ACCEL - o2.Z_ACCEL;});
		this.safetyStatus = SafetyStatus.Safe;
	}
	
	public synchronized void pushData(SensorAccelData data)
	{
		
		this.dataQueue.add(data);

		this.xSortedList.add(data);
		this.ySortedList.add(data);
		this.zSortedList.add(data);

		if(dataQueue.size() < 2)
		{
			return;
		}
		
		int xdiff = this.xSortedList.peekLast().X_ACCEL - this.xSortedList.peekFirst().X_ACCEL;
		int ydiff = this.ySortedList.peekLast().Y_ACCEL - this.ySortedList.peekFirst().Y_ACCEL;
		int zdiff = this.zSortedList.peekLast().Z_ACCEL - this.zSortedList.peekFirst().Z_ACCEL;
		//System.out.printf("X:%d, Y:%d, Z:%d 비교대상:%d XMax:%d, XMin:%d\n", xdiff, ydiff, zdiff, dataQueue.size(), this.xSortedList.peekFirst().X_ACCEL, this.xSortedList.peekLast().X_ACCEL);
		if(xdiff > Threshold || ydiff > Threshold || zdiff > Threshold)
		{
			if(this.safetyStatus == SafetyStatus.Safe)
			{
				this.safetyStatus = SafetyStatus.Danger;
				SafeStateChangeEvent event = new SafeStateChangeEvent(this.sensor, this.safetyStatus);
				this.manager.notifyObservers(ServerCore.mainThreadPool, event);
				logger.log(Level.INFO, this.sensor.ID+" 센서 안전상태");
			}
		}
		else
		{
			if(this.safetyStatus == SafetyStatus.Danger)
			{
				this.safetyStatus = SafetyStatus.Safe;
				SafeStateChangeEvent event = new SafeStateChangeEvent(this.sensor, this.safetyStatus);
				this.manager.notifyObservers(ServerCore.mainThreadPool, event);
				logger.log(Level.INFO, this.sensor.ID+" 센서 위험상태");
			}
		}
		SensorAccelData peekData = this.dataQueue.peek();
		if(peekData != null)
		{
			while(data.time.getTime() - peekData.time.getTime() > this.configAccess.getAnalyseInterval())
			{
				this.dataQueue.poll();
				this.xSortedList.remove(peekData);
				this.ySortedList.remove(peekData);
				this.zSortedList.remove(peekData);
				peekData = this.dataQueue.peek();
			}
		}
	}

	public SafetyStatus getState()
	{
		return this.safetyStatus;
	}

}
