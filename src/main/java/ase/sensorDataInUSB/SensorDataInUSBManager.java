package ase.sensorDataInUSB;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.console.LogWriter;
import ase.hardware.GPIOControl;

public class SensorDataInUSBManager
{
	public static final Logger logger = LogWriter.createLogger(SensorDataInUSBManager.class, "SensorDataInUSB");
	
	private GpioPinListenerDigital btnListener;
	
	public SensorDataInUSBManager()
	{
		this.btnListener = this::btnListener;
	}
	
	private void btnListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getState().isHigh())
		{
			logger.log(Level.INFO, "USB마운트");
		}
		
	}
	
	public boolean startModule()
	{
		GPIOControl.inst().btn1.addListener(this.btnListener);
		return true;
	}
	
	public void stopModule()
	{
		GPIOControl.inst().btn1.removeListener(this.btnListener);
	}
}
