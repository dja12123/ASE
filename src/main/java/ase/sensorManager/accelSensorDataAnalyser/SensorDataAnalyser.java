package ase.sensorManager.accelSensorDataAnalyser;

import java.util.LinkedList;
import java.util.Queue;

import ase.sensorManager.SensorConfigAccess;
import ase.sensorManager.sensorDataAccel.SensorAccelData;
import ase.util.SortedLinkedList;

public class SensorDataAnalyser
{
	private final SensorConfigAccess configAccess;
	private final Queue<SensorAccelData> dataQueue;
	
	private SortedLinkedList<SensorAccelData> xSortedList;
	private SortedLinkedList<SensorAccelData> ySortedList;
	private SortedLinkedList<SensorAccelData> zSortedList;

	
	public SensorDataAnalyser(SensorConfigAccess configAccess)
	{
		this.configAccess = configAccess;
		this.dataQueue = new LinkedList<>();

		this.xSortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.X_ACCEL - o2.X_ACCEL;});
		this.ySortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.Y_ACCEL - o2.Y_ACCEL;});
		this.zSortedList = new SortedLinkedList<>((SensorAccelData o1, SensorAccelData o2)->{ return o1.Z_ACCEL - o2.Z_ACCEL;});
	}
	
	public void pushData(SensorAccelData data)
	{
		this.dataQueue.add(data);

		this.xSortedList.add(data);
		this.ySortedList.add(data);
		this.zSortedList.add(data);
		System.out.println("block0");
		if(dataQueue.size() < 2)
		{
			return;
		}
		int xdiff = this.xSortedList.peekLast().X_ACCEL - this.xSortedList.peekFirst().X_ACCEL;
		int ydiff = this.xSortedList.peekLast().Y_ACCEL - this.xSortedList.peekFirst().Y_ACCEL;
		int zdiff = this.xSortedList.peekLast().Z_ACCEL - this.xSortedList.peekFirst().Z_ACCEL;
		System.out.println("block1");
		for(SensorAccelData d : this.xSortedList)
		{
			System.out.printf("%d ",d.X_ACCEL);
		}
		System.out.println("block2");
		System.out.println();
		System.out.printf("X:%d, Y:%d, Z:%d 비교대상:%d XMax:%d, XMin:%d", xdiff, ydiff, zdiff, dataQueue.size(), this.xSortedList.peekFirst().X_ACCEL, this.xSortedList.peekLast().X_ACCEL);
		System.out.println();
		
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
		System.out.println("block3");
	}
	


}
