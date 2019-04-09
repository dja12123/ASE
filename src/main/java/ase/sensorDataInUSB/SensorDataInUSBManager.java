package ase.sensorDataInUSB;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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
import ase.sensorManager.sensor.DataReceiveEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorDataInUSBManager
{
	
	public static void main(String[] args) throws Exception
	{
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
		File[] files = new File("D:\\Users\\dja12123\\Documents\\반디카메라").listFiles();
		List<TempClass> fileSortList = new LinkedList<>();
		for(File f : files)
		{

			TempClass t = new TempClass();
			BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
			t.date = new Date(attr.creationTime().toMillis());
			t.file = f;
			fileSortList.add(t);
		}
		Collections.sort(fileSortList);
		for(TempClass t : fileSortList)
		{
			System.out.println(FileDateFormat.format(t.date));
		}
	}
	public static final String PROP_USB_DEVICE = "UsbDevice";
	public static final String PROP_USB_MOUNT_DIR = "MountDir";
	private static final String PROP_SAVE_TASK_INTERVAL = "SaveTaskInterval";
	private static final String PROP_FREE_CAP_KB = "FreeCapKB";
	private static final SimpleDateFormat FileDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static final String FilePrefix = "SensorData";
	private static final String FilePostfix = ".csv";
	
	public static final Logger logger = LogWriter.createLogger(SensorDataInUSBManager.class, "SensorDataInUSB");
	
	private final SensorManager sensorManager;
	private final Observer<DataReceiveEvent> sensorDataReceiveObserver;
	private final Runnable task;
	
	private GpioPinListenerDigital btnListener;
	private String usbDevice;
	private File mountFile;
	private int freeCapKB;
	private String mountDir;
	private String fullMountDir;
	private boolean mountingTask;
	private boolean ismount;
	private Queue<DataReceiveEvent> sensorDataQueue;
	private Thread taskThread;
	private boolean isRun;
	private int saveTaskInterval;
	private int usbCapKB;
	private File taskFile;
	
	private DisplayObject dispUsbState;
	private DisplayObject dispCapacity;

	public SensorDataInUSBManager(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		this.btnListener = this::btnListener;
		this.sensorDataReceiveObserver = this::sensorDataReceiveObserver;
		this.task = this::task;
		this.mountingTask = false;
		this.ismount = false;
		this.sensorDataQueue = new LinkedList<>();
	}
	
	private void btnListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getState().isHigh())
		{
			ServerCore.mainThreadPool.execute(()->
			{
				if(this.ismount)
				{
					this.stopTask();
					this.recordData();
					this.unMount();
				}
				else
				{
					this.mount();
					this.startTask();
				}
			});
		}
	}
	
	public boolean startModule()
	{
		logger.log(Level.INFO, "USB 센서 정보 저장기 시작");
		GPIOControl.inst().btn1.addListener(this.btnListener);
		this.usbDevice = ServerCore.getProp(PROP_USB_DEVICE);
		this.mountDir = ServerCore.getProp(PROP_USB_MOUNT_DIR);
		this.fullMountDir = FileHandler.getExtResourceFile(this.mountDir).toString();
		this.saveTaskInterval = Integer.parseInt(ServerCore.getProp(PROP_SAVE_TASK_INTERVAL));
		this.freeCapKB = Integer.parseInt(ServerCore.getProp(PROP_FREE_CAP_KB));
		this.ismount = this.checkMount();
		this.dispUsbState = DisplayControl.inst().showString(70, 0, "usb:");
		this.dispCapacity = DisplayControl.inst().showString(70, 15, " ");
		if(this.ismount)
		{
			logger.log(Level.INFO, "USB마운트 확인 " + this.usbDevice);
			this.mountFile = FileHandler.getExtResourceFile(this.mountDir);
			this.startTask();
		}
		else
		{
			this.mount();
		}
		this.displayMount();
		this.sensorManager.publicDataReceiveObservable.addObserver(this.sensorDataReceiveObserver);

		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "USB 센서 정보 저장기 종료");
		this.sensorManager.publicDataReceiveObservable.removeObserver(this.sensorDataReceiveObserver);
		DisplayControl.inst().removeShape(this.dispUsbState);
		this.stopTask();
		this.recordData();
		this.unMount();
		this.sensorDataQueue.clear();
		GPIOControl.inst().btn1.removeListener(this.btnListener);

	}
	
	private void startTask()
	{
		if(this.isRun) return;
		this.taskThread = new Thread(this.task);
		this.taskThread.setDaemon(true);
		this.isRun = true;
		this.taskThread.start();
	}
	
	private void stopTask()
	{
		if(!this.isRun) return;
		this.isRun = false;
		if(this.taskThread != null) this.taskThread.interrupt();
		this.usbCapKB = 0;
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
			int freeSpace = this.usbCapKB - this.getUseSpaceKB();
			if(freeSpace <= this.freeCapKB)
			{
				logger.log(Level.WARNING, "기록 실패: USB가 가득 참");
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
				bufferedWriter.write(String.format("%d,%f,%f,%f,%f,%f,%f"
						, data.sensorInst.id
						, data.data.X_GRADIANT
						, data.data.Y_GRADIANT
						, data.data.X_ACCEL
						, data.data.Y_ACCEL
						, data.data.Z_ACCEL
						, data.data.Altitiude));
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
		while(this.isRun)
		{
			logger.log(Level.INFO, "센서 정보 저장");
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
		this.taskFile = null;
	}
	
	private File getTaskFile()
	{
		if(this.taskFile != null)
		{
			String fileName = this.taskFile.getName();
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
				return this.taskFile;
			}
		}
		this.taskFile = new File(this.mountFile, FilePrefix+"_"+FileDateFormat.format(new Date())+"_"+FilePostfix);
		return this.taskFile;
	}
	
	private boolean checkAndRemoveFile()
	{
		int freeSpace = this.usbCapKB - this.getUseSpaceKB();
		if(freeSpace > this.freeCapKB)
		{
			return false;
		}
		File[] files = this.mountFile.listFiles();
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
				if(freeSpaceByte > this.freeCapKB * 1024)
				{
					return true;
				}
			}
		}
		return true;
	}
	
	public synchronized void mount()
	{
		if(this.mountingTask || this.ismount) return;
		this.mountingTask = true;
		logger.log(Level.INFO, "USB마운트 " + this.usbDevice);
		try
		{
			String result = CommandExecutor.executeCommand(String.format("mount %s %s", 
					this.usbDevice, this.fullMountDir));
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
			this.usbCapKB = this.getTotalSpaceKB();
			this.mountFile = FileHandler.getExtResourceFile(this.mountDir);
		}
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
		logger.log(Level.INFO, "USB언마운트 " + this.usbDevice + " " + this.mountFile.toString());
		try
		{
			String result = CommandExecutor.executeCommand(String.format("umount %s", 
					this.usbDevice));
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
			this.mountFile = null;
		}
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
			if(target.equals(this.fullMountDir))
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
			this.dispCapacity = DisplayControl.inst().replaceString(this.dispCapacity, String.format("%.1fGB", (double)(this.usbCapKB - this.getUseSpaceKB()) / (1024*1024)));
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
			result = CommandExecutor.executeCommand(String.format("df %s --output=size", this.usbDevice));
			result = result.split("\n")[1];
			result = result.trim();
			totalSize = Integer.parseInt(result);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "용량정보 오류", e);
		}

		return totalSize;
	}
	
	private int getUseSpaceKB()
	{
		int useSize = 0;
		String result;
		try
		{
			result = CommandExecutor.executeCommand(String.format("df %s --output=used", this.usbDevice));
			result = result.split("\n")[1];
			result = result.trim();
			useSize = Integer.parseInt(result);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "용량정보 오류", e);
		}

		return useSize;
	}
	
	private void sensorDataReceiveObserver(Observable<DataReceiveEvent> provider, DataReceiveEvent e)
	{
		this.sensorDataQueue.add(e);
	}
}
