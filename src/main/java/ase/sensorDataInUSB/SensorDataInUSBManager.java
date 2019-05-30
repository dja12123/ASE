package ase.sensorDataInUSB;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
import ase.sensorManager.SensorManager;
import ase.sensorManager.sensorData.DataReceiveEvent;
import ase.util.observer.Observer;

public class SensorDataInUSBManager
{
	public static final String PROP_USB_DEVICE = "UsbDevice";
	public static final String PROP_USB_MOUNT_DIR = "MountDir";
	private static final String PROP_SAVE_TASK_INTERVAL = "SaveTaskInterval";
	private static final String PROP_FREE_CAP_KB = "FreeCapKB";
	private static final SimpleDateFormat FileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat DataDateFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
	private static final String FilePrefix = "SensorData";
	private static final String FilePostfix = ".csv";
	
	public static final Logger logger = LogWriter.createLogger(SensorDataInUSBManager.class, "SensorDataInUSB");
	
	private final SensorManager sensorManager;
	private final Observer<DataReceiveEvent> sensorDataReceiveObserver;
	private final Runnable task;
	
	private String propUsbDevice;
	private int propFreeCapKB;
	private String propMountDir;
	private GpioPinListenerDigital btnListener;
	private File mountDir;
	private String mountDirStr;
	private boolean isMountingTask;
	private boolean ismounted;
	private Queue<DataReceiveEvent> sensorDataQueue;
	private Thread taskThread;
	private boolean isRunSaveTask;
	private int saveTaskInterval;
	private int usbFullCapKB;
	private File nowTaskFile;
	
	private DisplayObject dispUsbState;
	private DisplayObject dispCapacity;

	public SensorDataInUSBManager(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this.btnListener = this::btnListener;
		this.sensorDataReceiveObserver = this::sensorDataReceiveObserver;
		this.task = this::task;
		this.isMountingTask = false;
		this.ismounted = false;
		this.sensorDataQueue = new LinkedList<>();
	}
	
	private void btnListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getState().isHigh())
		{
			ServerCore.mainThreadPool.execute(()->
			{
				if(this.ismounted)
				{
					if(this.ismounted) this.recordData();
					this.stopTask();
					this.unMount();
				}
				else
				{
					this.mount();
					if(this.ismounted) this.startTask();
				}
			});
		}
	}
	
	public boolean startModule()
	{
		logger.log(Level.INFO, "USB 센서 정보 저장기 시작");
		GPIOControl.inst().btn1.addListener(this.btnListener);
		this.propUsbDevice = ServerCore.getProp(PROP_USB_DEVICE);
		this.propMountDir = ServerCore.getProp(PROP_USB_MOUNT_DIR);
		this.mountDirStr = FileHandler.getExtResourceFile(this.propMountDir).toString();
		this.saveTaskInterval = Integer.parseInt(ServerCore.getProp(PROP_SAVE_TASK_INTERVAL));
		this.propFreeCapKB = Integer.parseInt(ServerCore.getProp(PROP_FREE_CAP_KB));
		this.ismounted = this.checkMount();
		this.dispUsbState = DisplayControl.inst().showString(70, 0, "usb:");
		this.dispCapacity = DisplayControl.inst().showString(70, 15, " ");
		if(this.ismounted)
		{
			logger.log(Level.INFO, "USB마운트 확인 " + this.propUsbDevice);
			this.mountDir = FileHandler.getExtResourceFile(this.propMountDir);
			this.usbFullCapKB = this.getTotalSpaceKB();
			this.startTask();
		}
		else
		{
			this.mount();
			if(this.ismounted) this.startTask();
		}
		
		this.displayMount();
		

		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "USB 센서 정보 저장기 종료");
		DisplayControl.inst().removeShape(this.dispUsbState);
		this.recordData();
		this.stopTask();
		this.unMount();
		this.sensorDataQueue.clear();
		GPIOControl.inst().btn1.removeListener(this.btnListener);

	}
	
	private void startTask()
	{
		if(this.isRunSaveTask) return;
		this.sensorManager.dataManager.addObserver(this.sensorDataReceiveObserver);
		this.taskThread = new Thread(this.task);
		this.taskThread.setDaemon(true);
		this.isRunSaveTask = true;
		this.taskThread.start();
	}
	
	private void stopTask()
	{
		if(!this.isRunSaveTask) return;
		this.sensorManager.dataManager.removeObserver(this.sensorDataReceiveObserver);
		this.isRunSaveTask = false;
		if(this.taskThread != null) this.taskThread.interrupt();
		this.usbFullCapKB = 0;
	}
	
	private boolean recordData()
	{
		File taskFile = this.getTaskFile();
		if(taskFile == null)
		{
			logger.log(Level.SEVERE, "기록 오류: 잘못된 파일 이름");
			return false;
		}
		if(this.checkAndRemoveFile())
		{
			int freeSpace = this.usbFullCapKB - this.getUseSpaceKB();
			if(freeSpace <= this.propFreeCapKB)
			{
				logger.log(Level.WARNING, "기록 실패: USB가 가득 참 free: "+freeSpace+"KB");
				return false;
			}
		}
		if(!(taskFile.exists() && taskFile.isFile()))
		{
			try
			{
				if(!taskFile.createNewFile())
				{
					logger.log(Level.WARNING, "기록 파일 생성 실패"+taskFile.toString());
					return false;
				}		
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, taskFile.getParent());
				logger.log(Level.WARNING, "기록 파일 생성 실패"+taskFile.toString(), e);
				return false;
			}
		}
		BufferedWriter bufferedWriter;
		try
		{
			bufferedWriter = new BufferedWriter(new FileWriter(taskFile, true));
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "기록 오류", e);
			return false;
		}


		while(!this.sensorDataQueue.isEmpty())
		{
			DataReceiveEvent data = this.sensorDataQueue.poll();
			try
			{
				bufferedWriter.write(String.format("%s,%d,%d,%d,%d"
						, DataDateFormat.format(data.data.time)
						, data.sensorInst.ID
						, data.data.X_ACCEL
						, data.data.Y_ACCEL
						, data.data.Z_ACCEL));
				bufferedWriter.newLine();
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "기록 오류", e);
				break;
			}
		}
		try
		{
			bufferedWriter.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "파일 닫는중 오류", e);
			return false;
		}
		return true;
	}
	
	private void task()
	{
		while(this.isRunSaveTask)
		{
			if(!this.recordData()) this.stopTask();
			try
			{
				Thread.sleep(this.saveTaskInterval);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
	
	private File getTaskFile()
	{
		if(this.nowTaskFile != null)
		{
			String fileName = this.nowTaskFile.getName();
			Date fileDate;
			Date nowDate;
			try
			{
				fileDate = FileDateFormat.parse(fileName.split("_")[1]);
				nowDate = FileDateFormat.parse(FileDateFormat.format(new Date()));
			}
			catch (ParseException e)
			{
				logger.log(Level.WARNING, "작업 파일 가져오는중 오류", e);
				return null;
			}
			if(nowDate.compareTo(fileDate) == 0)
			{
				return this.nowTaskFile;
			}
		}
		this.nowTaskFile = new File(this.mountDir, FilePrefix+"_"+FileDateFormat.format(new Date())+"_"+FilePostfix);
		if(this.nowTaskFile == null)
		{
			logger.log(Level.WARNING, "작업 파일 가져오는중 오류");
		}
		return this.nowTaskFile;
	}
	
	private boolean checkAndRemoveFile()
	{
		int freeSpace = this.usbFullCapKB - this.getUseSpaceKB();
		if(freeSpace > this.propFreeCapKB)
		{
			return false;
		}
		if(this.mountDir == null) return false;
		File[] files = this.mountDir.listFiles();
		class TempClass implements Comparable<TempClass>
		{
			File file;
			Date date;
			@Override
			public int compareTo(TempClass o)
			{
				return date.compareTo(o.date);
			}
		}
		List<TempClass> fileSortList = new LinkedList<>();
		for(File f : files)
		{
			String[] nameArr = f.getName().split("_");
			if(nameArr.length != 3)
			{
				continue;
			}
			Date fileDate;
			try
			{
				fileDate = FileDateFormat.parse(nameArr[1]);
			}
			catch (ParseException e)
			{
				continue;
			}
			TempClass t = new TempClass();
			t.date = fileDate;
			t.file = f;
			fileSortList.add(t);
		}
		long freeSpaceByte = freeSpace * 1024;
		Collections.sort(fileSortList);
		while(!fileSortList.isEmpty())
		{
			TempClass c = fileSortList.get(0);
			long fileLength = c.file.length();
			if(c.file.exists() && c.file.delete())
			{
				freeSpaceByte += fileLength;
				if(freeSpaceByte > this.propFreeCapKB * 1024)
				{
					return true;
				}
			}
		}
		return true;
	}
	
	public synchronized void mount()
	{
		if(this.isMountingTask || this.ismounted) return;
		this.isMountingTask = true;
		logger.log(Level.INFO, "USB마운트 " + this.propUsbDevice);
		try
		{
			String result = CommandExecutor.executeCommand(String.format("mount %s %s", 
					this.propUsbDevice, this.mountDirStr));
			if(!result.isEmpty()) logger.log(Level.WARNING, result);
			Thread.sleep(200);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "마운트 실패", e);
		}
		boolean result = this.checkMount();
		if(result)
		{
			this.usbFullCapKB = this.getTotalSpaceKB();
			this.mountDir = FileHandler.getExtResourceFile(this.propMountDir);
		}
		if(result != this.ismounted)
		{
			this.ismounted = result;
			this.displayMount();
		}
		this.isMountingTask = false;
	}
	
	public synchronized void unMount()
	{
		if(this.isMountingTask || !this.ismounted) return;
		this.isMountingTask = true;
		logger.log(Level.INFO, "USB언마운트 " + this.propUsbDevice + " " + this.mountDir.toString());
		try
		{
			String result = CommandExecutor.executeCommand(String.format("umount %s", 
					this.propUsbDevice));
			if(!result.isEmpty()) logger.log(Level.WARNING, result);
			Thread.sleep(200);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "언마운트 실패", e);
		}
		boolean result = this.checkMount();
		if(!result)
		{
			this.mountDir = null;
		}
		if(result != this.ismounted)
		{
			this.ismounted = result;
			this.displayMount();
		}
		this.isMountingTask = false;
	}
	
	public boolean checkMount()
	{
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("findmnt -J -S %s", this.propUsbDevice));
			
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
			if(target.equals(this.mountDirStr))
			{
				return true;
			}
		}
		return false;
	}
	
	public void displayMount()
	{
		if(this.ismounted)
		{
			this.dispUsbState = DisplayControl.inst().replaceString(this.dispUsbState, "usb:run");
			this.dispCapacity = DisplayControl.inst().replaceString(this.dispCapacity, String.format("%.1fGB", (double)(this.usbFullCapKB - this.getUseSpaceKB()) / (1024*1024)));
		}
		else
		{
			this.dispUsbState = DisplayControl.inst().replaceString(this.dispUsbState, "usb:off");
			this.dispCapacity = DisplayControl.inst().replaceString(this.dispCapacity, " ");
		}
	}
	
	private int getTotalSpaceKB()
	{
		int totalSize = 0;
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("df %s --output=size", this.propUsbDevice));
			result = result.split("\n")[1];
			result = result.trim();
			totalSize = Integer.parseInt(result);
		}
		catch (Exception e)
		{
			
		}

		return totalSize;
	}
	
	private int getUseSpaceKB()
	{
		int useSize = 0;
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("df %s --output=used", this.propUsbDevice));
			result = result.split("\n")[1];
			result = result.trim();
			useSize = Integer.parseInt(result);
		}
		catch (Exception e)
		{
			
		}

		return useSize;
	}
	
	private void sensorDataReceiveObserver(DataReceiveEvent e)
	{
		this.sensorDataQueue.add(e);
	}
}
