package ase.sensorDataInUSB;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.ServerCore;
import ase.bash.CommandExecutor;
import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import ase.hardware.GPIOControl;

public class SensorDataInUSBManager
{
	public static final String PROP_USB_DEVICE = "UsbDevice";
	public static final String PROP_USB_MOUNT_DIR = "MountDir";
	
	public static final Logger logger = LogWriter.createLogger(SensorDataInUSBManager.class, "SensorDataInUSB");
	
	private GpioPinListenerDigital btnListener;
	private String usbDevice;
	private File mountDir;
	private boolean mountingTask;
	private boolean ismount;
	
	public SensorDataInUSBManager()
	{
		this.btnListener = this::btnListener;
		this.mountingTask = false;
		this.ismount = false;
	}
	
	private void btnListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getState().isHigh())
		{
			if(this.ismount) this.unMount();
			else this.mount();
		}
		
	}
	
	public boolean startModule()
	{
		GPIOControl.inst().btn1.addListener(this.btnListener);
		this.usbDevice = ServerCore.getProp(PROP_USB_DEVICE);
		this.mountDir = FileHandler.getExtResourceFile(ServerCore.getProp(PROP_USB_MOUNT_DIR));
		return true;
	}
	
	public void stopModule()
	{
		GPIOControl.inst().btn1.removeListener(this.btnListener);
	}
	
	public synchronized void mount()
	{
		if(this.mountingTask) return;
		this.mountingTask = true;
		logger.log(Level.INFO, "USB마운트 " + this.usbDevice + " " + this.mountDir.toString());
		try
		{
			CommandExecutor.executeCommand(String.format("mount %s %s", 
					this.usbDevice, this.mountDir.toString()));
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
			this.ismount = false;
			this.mountingTask = false;
			return;
		}
		this.ismount = true;
		this.mountingTask = false;
	}
	
	public synchronized void unMount()
	{
		if(this.mountingTask) return;
		this.mountingTask = true;
		logger.log(Level.INFO, "USB언마운트 " + this.usbDevice + " " + this.mountDir.toString());
		try
		{
			CommandExecutor.executeCommand(String.format("umount %s", 
					this.usbDevice));
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
		}
		this.ismount = false;
		this.mountingTask = false;
	}
}
