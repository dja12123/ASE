package ase.hardware;

public class DisplayDeviceState
{
	private static final int UPDATE_INTERVAL = 1000;
	private Thread taskThread;
	private boolean isRun;
	private Runnable task;
	
	public DisplayDeviceState()
	{
		this.task = this::task;
	}
	
	private boolean startModule()
	{
		this.taskThread = new Thread(this.task);
		this.taskThread.start();
		return true;
	}
	
	private void stopModule()
	{
		if(this.taskThread != null) this.taskThread.interrupt();
		this.isRun = false;
	}
	
	private void task()
	{
		while(this.isRun)
		{
			
			
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
