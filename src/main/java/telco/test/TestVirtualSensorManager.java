package telco.test;

import java.util.ArrayList;

import telco.sensorReadServer.sensorManager.SensorManager;

public class TestVirtualSensorManager
{
	public static final int SENSOR_DATA_SEND_INTERVAL = 1000;
	
	private SensorManager sensorManager;
	private ArrayList<TestVirtualSensor> virtualSensorList;
	
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
	}
	
	public void stop()
	{
		for(TestVirtualSensor vs: this.virtualSensorList)
		{
			vs.stop();
		}
		this.virtualSensorList.clear();
	}
}
