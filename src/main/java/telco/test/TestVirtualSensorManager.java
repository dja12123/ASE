package telco.test;

import java.util.ArrayList;
import java.util.Random;

import telco.sensorReadServer.sensorManager.SensorManager;

public class TestVirtualSensorManager
{
	public static final int SENSOR_DATA_SEND_INTERVAL = 1500;
	
	private SensorManager sensorManager;
	private ArrayList<TestVirtualSensor> virtualSensorList;
	private Thread thread;
	
	private int randomRemoveSensor = -1;
	
	public TestVirtualSensorManager(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this.virtualSensorList = new ArrayList<>();
		
	}
	
	public void start()
	{
		for(int i = 9000; i < 9010; ++i)
		{
			TestVirtualSensor vs = new TestVirtualSensor(i, this.sensorManager);
			this.virtualSensorList.add(vs);
		}
		
		this.thread = new Thread(this::run);
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	private void run()
	{
		Random r = new Random();
		while(true)
		{
			int member = r.nextInt(this.virtualSensorList.size());
			this.virtualSensorList.get(member).sleep();
			if(randomRemoveSensor == -1)
			{
				member = r.nextInt(this.virtualSensorList.size());
				
				this.sensorManager.removeSensor(this.virtualSensorList.get(member).id);
				this.virtualSensorList.get(member).sleep();
			}
			else
			{
				randomRemoveSensor = -1;
			}
			try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
	
	public void stop()
	{
		this.thread.interrupt();
		for(TestVirtualSensor vs: this.virtualSensorList)
		{
			vs.stop();
		}
		this.virtualSensorList.clear();
	}
}
