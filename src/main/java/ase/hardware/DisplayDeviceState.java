package ase.hardware;

import ase.clientSession.ClientSessionManager;
import ase.clientSession.SessionEvent;
import ase.sensorManager.SensorManager;
import ase.sensorManager.SensorRegisterEvent;
import ase.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observer;

public class DisplayDeviceState
{
	private static final int GRAPH_WIDTH = 40;
	
	private final Observer<DeviceStateEvent> deviceStateObserver;
	private final Observer<SensorRegisterEvent> sensorObserver;
	private final Observer<SensorOnlineEvent> sensorOnlineObserver;
	private final Observer<SessionEvent> sessionObserver;
	private final SensorManager sensorManager;
	private final ClientSessionManager appManager;
	
	private DisplayObject strCpu;
	private DisplayObject barCpu;
	private int cpuPixel;
	
	private DisplayObject dispMem;
	private DisplayObject barMem;
	private int memPixel;
	
	private DisplayObject strSensorInfo;
	private DisplayObject strAppInfo;
	
	public DisplayDeviceState(SensorManager sensorManager, ClientSessionManager appManager)
	{
		this.deviceStateObserver = this::deviceStateObserver;
		this.sensorObserver = this::sensorObserver;
		this.sensorOnlineObserver = this::sensorOnlineObserver;
		this.sessionObserver = this::sessionObserver;
		this.sensorManager = sensorManager;
		this.appManager = appManager;
	}
	
	public boolean startModule()
	{
		this.strCpu = DisplayControl.inst().showString(0, 0, "CPU");
		this.barCpu = DisplayControl.inst().showRect(23, 0, GRAPH_WIDTH, 12);
		this.dispMem = DisplayControl.inst().showString(0, 13, "MEM");
		this.barMem = DisplayControl.inst().showRect(23, 13, GRAPH_WIDTH, 12);
		this.strSensorInfo = DisplayControl.inst().showString(0, 26, 
				String.format("sensor:%d on:%d"
						, this.sensorManager.sensorMap.size()
						, this.sensorManager.getOnlineSensorCount()));
		this.strAppInfo = DisplayControl.inst().showString(0, 39, String.format("user:%d"
				, this.appManager.getSessionCount()));
		
		DeviceStateMonitor.inst().addObserver(this.deviceStateObserver);
		this.sensorManager.addObserver(this.sensorObserver);
		this.sensorManager.publicSensorOnlineObservable.addObserver(this.sensorOnlineObserver);
		this.appManager.addObserver(this.sessionObserver);
		return true;
	}
	
	public void stopModule()
	{
		DeviceStateMonitor.inst().removeObserver(this.deviceStateObserver);
		this.sensorManager.removeObserver(this.sensorObserver);
		this.sensorManager.publicSensorOnlineObservable.removeObserver(this.sensorOnlineObserver);
		this.appManager.removeObserver(this.sessionObserver);
		
		DisplayControl.inst().removeShape(this.strCpu);
		DisplayControl.inst().removeShape(this.barCpu);
		DisplayControl.inst().removeShape(this.dispMem);
		DisplayControl.inst().removeShape(this.barMem);
		DisplayControl.inst().removeShape(this.strSensorInfo);
		DisplayControl.inst().removeShape(this.strAppInfo);
	}
	
	private void deviceStateObserver(DeviceStateEvent event)
	{
		int cpuPixel = (int) (event.cpuLoad * GRAPH_WIDTH);
		int memPixel = (int) (((double)event.useMemByte / (double)event.totalMemByte) * GRAPH_WIDTH);
		if(cpuPixel != this.cpuPixel)
		{
			this.barCpu = DisplayControl.inst().replaceShape(this.barCpu, this.getBar(GRAPH_WIDTH, 12, cpuPixel));
			this.cpuPixel = cpuPixel;
		}
		if(memPixel != this.memPixel)
		{
			this.barMem = DisplayControl.inst().replaceShape(this.barMem, this.getBar(GRAPH_WIDTH, 12, memPixel));
			this.memPixel = memPixel;
		}
	}
	
	private void sensorObserver(SensorRegisterEvent event)
	{
		this.strSensorInfo = DisplayControl.inst().replaceString(this.strSensorInfo, 
				String.format("sensor:%d on:%d", this.sensorManager.sensorMap.size(), this.sensorManager.getOnlineSensorCount()));
	}
	
	private void sensorOnlineObserver(SensorOnlineEvent event)
	{
		this.strSensorInfo = DisplayControl.inst().replaceString(this.strSensorInfo, 
				String.format("sensor:%d on:%d", this.sensorManager.sensorMap.size(), this.sensorManager.getOnlineSensorCount()));
	}
	
	private void sessionObserver(SessionEvent event)
	{
		this.strAppInfo = DisplayControl.inst().replaceString(this.strAppInfo, 
				String.format("user:%d", this.appManager.getSessionCount()));
	}
	
	private boolean[][] getBar(int width, int height, int fill)
	{
		boolean[][] result = new boolean[width][height];
		for(int i = 0; i < width; ++i)
		{
			result[i][0] = true;
			result[i][height - 1] = true;
		}
		for(int i = 0; i < height; ++i)
		{
			result[0][i] = true;
			result[width - 1][i] = true;
		}
		for(int i = 1; i < fill; ++i)
		{
			for(int j = 1; j < height; ++j)
			{
				result[i][j] = true;
			}
		}
		
		return result;
	}
	
	
}
