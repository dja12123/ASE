package ase.hardware;

public class DeviceStateEvent
{
	public final double cpuLoad;
	public final long totalMemByte;
	public final long useMemByte;
	
	public DeviceStateEvent(double cpuLoad, long totalMemByte, long useMemByte)
	{
		this.cpuLoad = cpuLoad;
		this.totalMemByte = totalMemByte;
		this.useMemByte = useMemByte;
	}
	
	public long getTotalMemKB()
	{
		return this.totalMemByte / 1024;
	}
	
	public long getTotalMemMB()
	{
		return this.totalMemByte / 1024 / 1024;
	}

	public long getTotalMemGB()
	{
		return this.totalMemByte / 1024 / 1024 / 1024;
	}
	
	public long getUseMemKB()
	{
		return this.useMemByte / 1024;
	}
	
	public long getUseMemMB()
	{
		return this.useMemByte / 1024 / 1024;
	}

	public long getUseMemGB()
	{
		return this.useMemByte / 1024 / 1024 / 1024;
	}
}
