package ase.sensorDataInUSB;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.ServerCore;
import ase.bash.CommandExecutor;
import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import ase.hardware.DisplayControl;
import ase.hardware.DisplayObject;
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
	private DisplayObject dispUsbState;
	private DisplayObject dispCapacity;
	
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
		logger.log(Level.INFO, "USB 센서 정보 저장기 시작");
		GPIOControl.inst().btn1.addListener(this.btnListener);
		this.usbDevice = ServerCore.getProp(PROP_USB_DEVICE);
		this.mountDir = FileHandler.getExtResourceFile(ServerCore.getProp(PROP_USB_MOUNT_DIR));
		this.ismount = this.checkMount();
		this.dispUsbState = DisplayControl.inst().showString(70, 0, "usb:");
		this.dispCapacity = DisplayControl.inst().showString(70, 15, " ");
		if(this.ismount)
		{
			logger.log(Level.INFO, "USB마운트 확인 " + this.usbDevice + " " + this.mountDir.toString());
			
		}
		else
		{
			this.mount();
		}
		this.displayMount();
		
		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "USB 센서 정보 저장기 종료");
		DisplayControl.inst().removeShape(this.dispUsbState);
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
			Thread.sleep(200);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
		}
		boolean result = this.checkMount();
		if(result != this.ismount)
		{
			this.ismount = result;
			this.displayMount();
		}
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
			Thread.sleep(200);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
		}
		boolean result = this.checkMount();
		if(result != this.ismount)
		{
			this.ismount = result;
			this.displayMount();
		}
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
		JsonElement element = parser.parse(result);
		if(!(element instanceof JsonObject)) return false;
		JsonObject jsonObj = (JsonObject) element;
		JsonArray arr = jsonObj.getAsJsonArray("filesystems");
		for(int i = 0; i < arr.size(); ++i)
		{
			JsonObject obj = (JsonObject) arr.get(i);
			String target = obj.get("target").getAsString();
			if(target.equals(this.mountDir.toString()))
			{
				return true;
			}
		}
		return false;
	}
	
	public void displayMount()
	{
		if(this.ismount)
		{
			this.dispUsbState = DisplayControl.inst().replaceString(this.dispUsbState, "usb:run");
			this.dispCapacity = DisplayControl.inst().replaceString(this.dispCapacity, String.format("%.1fGB", this.getFreeSpaceGB()));
		}
		else
		{
			this.dispUsbState = DisplayControl.inst().replaceString(this.dispUsbState, "usb:off");
			this.dispCapacity = DisplayControl.inst().replaceString(this.dispCapacity, " ");
		}
	}
	
	public double getFreeSpaceGB()
	{
		double totalSize = 0;
		double useSize = 0;
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("df %s --output=size,used", this.usbDevice));
			result = result.split("\n")[1];
			String[] arr = result.split("\\s+");
			totalSize = Integer.parseInt(arr[0]);
			useSize = Integer.parseInt(arr[1]);
		}
		catch (Exception e)
		{
		}

		return (totalSize - useSize) / (1024 * 1024);
	}
}
