package ase.hardware;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import ase.util.observer.Observable;


public class DeviceStateMonitor extends Observable<DeviceStateEvent>
{
	private static DeviceStateMonitor inst;
	
	public static void init()
	{
		inst = new DeviceStateMonitor();
	}
	
	public static void destroy()
	{
		if(inst != null) inst.stopModule();
	}
	
	public static DeviceStateMonitor inst()
	{
		return inst;
	}
	
	private static final int UPDATE_INTERVAL = 1000;
	private OperatingSystemMXBean osbean;
	private Thread taskThread;
	private boolean isRun;
	private Runnable task;

	private DeviceStateMonitor()
	{
		this.task = this::task;
		this.osbean =  ManagementFactory.getOperatingSystemMXBean();
		this.isRun = true;
		this.taskThread = new Thread(this.task);
		this.taskThread.start();
	}

	private void stopModule()
	{
		if (this.taskThread != null)
			this.taskThread.interrupt();
		this.isRun = false;
	}

	private void task()
	{
		while (this.isRun)
		{
			if(this._observers.size() > 0)
			{
				double load = osbean.getSystemLoadAverage();
				long totalMem = Runtime.getRuntime().totalMemory();
				long useMem = Runtime.getRuntime().freeMemory();
				
				DeviceStateEvent event = new DeviceStateEvent(load, totalMem, useMem);
				this.notifyObservers(event);
			}

			try
			{
				Thread.sleep(UPDATE_INTERVAL);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
