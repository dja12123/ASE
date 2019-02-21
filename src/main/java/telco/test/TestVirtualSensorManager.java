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
		TestVirtualSensor s;
		Random r = new Random();
		while(true)
		{
			int index = r.nextInt(this.virtualSensorList.size());
			s = this.virtualSensorList.get(index);
			if(s.id != 9000) s.sleep();
			if(r.nextInt(3) == 2)
			{
				
				index = r.nextInt(this.virtualSensorList.size());
				
				s = this.virtualSensorList.get(index);
				if(s.id != 9000)
				{
					this.sensorManager.removeSensor(s.id);
					s.sleep();
				}
					
				

				
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
