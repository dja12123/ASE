package ase.sensorDataInUSB;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
			ServerCore.mainThreadPool.execute(()->
			{
				if(this.ismount) this.unMount();
				else this.mount();
			});
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
		if(this.mountingTask || this.ismount) return;
		this.mountingTask = true;
		logger.log(Level.INFO, "USB마운트 " + this.usbDevice + " " + this.mountDir.toString());
		try
		{
			String result = CommandExecutor.executeCommand(String.format("mount %s %s", 
					this.usbDevice, this.mountDir.toString()));
			if(!result.isEmpty()) logger.log(Level.WARNING, result);
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
			this.ismount = false;
			this.mountingTask = false;
			return;
		}
		this.checkMount();
		this.ismount = true;
		this.mountingTask = false;
	}
	
	public synchronized void unMount()
	{
		if(this.mountingTask || !this.ismount) return;
		this.mountingTask = true;
		logger.log(Level.INFO, "USB언마운트 " + this.usbDevice + " " + this.mountDir.toString());
		try
		{
			String result = CommandExecutor.executeCommand(String.format("umount %s", 
					this.usbDevice));
			if(!result.isEmpty()) logger.log(Level.WARNING, result);
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
		}
		this.checkMount();
		this.ismount = false;
		this.mountingTask = false;
	}
	
	public boolean checkMount()
	{
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("findmnt -J -S %s", this.usbDevice));
		}
		catch (Exception e)
		{
			return false;
		}
		JsonParser parser = new JsonParser();
		if(result == null) return false;
		JsonObject element = (JsonObject)parser.parse(result);
		System.out.println(element);
		return true;
	}
}
