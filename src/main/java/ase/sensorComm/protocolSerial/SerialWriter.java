package ase.sensorComm.protocolSerial;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.pi4j.io.serial.Serial;

public class SerialWriter
{
	private final Serial serial;
	
	private Thread serialWriteThread;
	private final Runnable serialWriteTask;
	private Queue<byte[]> writeQueue;

	private boolean isRun;
	
	public SerialWriter(Serial serial)
	{
		this.serial = serial;
		this.serialWriteTask = this::serialWriteTask;
		this.writeQueue = new LinkedList<>();
		
	}
	
	public synchronized void startModule()
	{
		this.isRun = true;
		this.serialWriteThread = new Thread(this.serialWriteTask);
		this.serialWriteThread.setDaemon(true);
		this.serialWriteThread.start();
	}
	
	public synchronized void stopModule()
	{
		if(this.isRun)
		{
			this.isRun = false;
			this.serialWriteThread.interrupt();
			this.writeQueue.clear();
		}
	}
	
	public synchronized void write(byte[] data)
	{
		this.writeQueue.add(data);
		this.notify();
	}
	
	private void serialWriteTask()
	{
		while(this.isRun)
		{
			while(!this.writeQueue.isEmpty())
			{
				byte[] packet;
				synchronized (this)
				{
					packet = this.writeQueue.poll();
				}
				try
				{
					synchronized (this.serial)
					{
						this.serial.write(packet);
					}
					
				}
				catch (IllegalStateException | IOException e)
				{
					e.printStackTrace();
				}
				try
				{
					Thread.sleep(SerialProtoDef.SERIAL_DELAY);
				}
				catch (InterruptedException e){ }
			}
			try
			{
				synchronized (this)
				{
					this.wait();
				}
			}
			catch (InterruptedException e){ }
		}
	}

}
