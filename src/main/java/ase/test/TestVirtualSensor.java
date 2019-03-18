package ase.test;

import java.util.Random;

import ase.sensorReadServer.sensorManager.SensorManager;

public class TestVirtualSensor
{
	public final int id;
	
	private SensorManager sensorManager;
	private Thread thread;
	private boolean sleepFlag;
	
	public TestVirtualSensor(int id, SensorManager sensorManager)
	{
		this.id = id;
		this.sensorManager = sensorManager;
		this.thread = new Thread(this::run);
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	public void sleep()
	{
		this.sleepFlag = true;
	}
	
	private void run()
	{
		Random r = new Random();
		while(true)
		{
			/*ByteBuffer buf = ByteBuffer.allocate(4+4+4+4+4+4+4+4);
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
			*/
			try
			{
				if(sleepFlag)
				{
					System.out.println("10초대기(타임아웃)");
					sleepFlag = false;
					Thread.sleep(100000);
				}
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
