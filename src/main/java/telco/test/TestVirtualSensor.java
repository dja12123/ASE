package telco.test;

import java.nio.ByteBuffer;
import java.util.Random;

import telco.sensorReadServer.sensorManager.SensorManager;
import telco.sensorReadServer.serialReader.DevicePacket;

public class TestVirtualSensor
{
	public final int id;
	
	private SensorManager sensorManager;
	private Thread thread;
	
	public TestVirtualSensor(int id, SensorManager sensorManager)
	{
		this.id = id;
		this.sensorManager = sensorManager;
		this.thread = new Thread(this::run);
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	private void run()
	{
		Random r = new Random();
		while(true)
		{
			ByteBuffer buf = ByteBuffer.allocate(4+4+4+4+4+4+4+4);
			buf.order(DevicePacket.BYTE_ORDER);
			buf.putInt(this.id);
			buf.putInt(32);
			
			buf.putFloat((r.nextFloat() * 10) + 10);
			buf.putFloat((r.nextFloat() * 10) + 10);
			
			buf.putFloat((r.nextFloat() * 10));
			buf.putFloat((r.nextFloat() * 10));
			buf.putFloat((r.nextFloat() * 10));
			
			buf.putFloat((r.nextFloat() * 10) + 5);
			
			DevicePacket dp = new DevicePacket(buf.array());
			this.sensorManager.update(null, dp);
			
			try
			{
				Thread.sleep(TestVirtualSensorManager.SENSOR_DATA_SEND_INTERVAL);
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
	}
	
}
