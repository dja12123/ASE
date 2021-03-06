package ase;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.appService.AppServiceManager;
import ase.bash.CommandExecutor;
import ase.clientSession.ClientSessionManager;
import ase.console.LogWriter;
import ase.db.DB_Handler;
import ase.db.DB_Installer;
import ase.fileIO.FileHandler;
import ase.hardware.Control;
import ase.hardware.DeviceStateMonitor;
import ase.hardware.DisplayControl;
import ase.hardware.DisplayDeviceState;
import ase.hardware.DisplayObject;
import ase.hardware.GPIOControl;
import ase.sensorAction.AccelSafetyControlManager;
import ase.sensorAction.O2SafetyControlManager;
import ase.sensorComm.protocolSerial.ProtocolSerial;
import ase.sensorDataInUSB.SensorDataInUSBManager;
import ase.sensorManager.SensorManager;
import ase.sensorReader.pureSerial.SerialReadManager;
import ase.sensorReader.tcpReader.TcpSensorReadManager;
import ase.test.TestVirtualSensorManager;
import ase.web.WebManager;

public class ServerCore
{
	private static final Properties properties = new Properties();
	public static final Logger logger = LogWriter.createLogger(ServerCore.class, "main");// 메인 로거
	public static final ExecutorService mainThreadPool = Executors.newCachedThreadPool();

	private static List<Thread> shutdownThreads;
	
	private static Thread mainThread;
	private static ServerCore mainInst;
	
	public static void main(String[] args)
	{
		if(!initProp())
		{
			return;
		}
		
		DisplayControl.init();
		GPIOControl.init();
		Control.init();
		DeviceStateMonitor.init();
		mainThread = Thread.currentThread();
		mainInst = new ServerCore();
		
		shutdownThreads = getShutdownHookList();
		
		for(Thread beforeShutdownThread : getShutdownHookList())
		{
			Runtime.getRuntime().removeShutdownHook(beforeShutdownThread);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(ServerCore::endProgram, "shutdownThread"));
		
		Thread shutdownThread = new Thread(ServerCore::endProgram, "shutdownThread");
		shutdownThread.setPriority(Thread.MAX_PRIORITY);
		
		if (!mainInst.start())
		{
			logger.log(Level.SEVERE, "초기화 실패");
			return;
		}
		Scanner sc = new Scanner(System.in);
		while(true)
		{
			String read = sc.nextLine();
			try
			{
				String[] arr = read.split("\\s+");
				int index = Integer.parseInt(arr[0]);
				if(index == 0)
				{
					mainInst.protocolSerial.getBroadcast().putSegment((short)Integer.parseInt(arr[1]));
				}
				else
				{
					mainInst.protocolSerial.getUserMap().get(index).putSegment((short)Integer.parseInt(arr[1]));
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			if(Thread.interrupted()) break;
		}
		sc.close();
		System.out.println("메인 쓰레드 종료");
	}
	
	public static void endProgram()
	{
		mainInst.shutdown();
		for(Thread nextShutdown : shutdownThreads)
		{
			nextShutdown.start();
		}
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			
		}
		mainThread.interrupt();
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String jvmName = bean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);
		try
		{
			CommandExecutor.executeCommand("kill -9 " + pid);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static List<Thread> getShutdownHookList()
	{
		try
		{
			Field hooksField = Class.forName("java.lang.ApplicationShutdownHooks").getDeclaredField("hooks");
			hooksField.setAccessible(true);

			@SuppressWarnings("unchecked")
			IdentityHashMap<Thread, Thread> currentHooks = (IdentityHashMap<Thread, Thread>) hooksField.get(null);
			List<Thread> hookList = new ArrayList<Thread>();
			hookList.addAll(currentHooks.keySet());
			return hookList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static void initMonitoringThread()
	{
		Thread monitoringThread = new Thread(()->{
			
			while(true)
			{
				Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
				long[] thread = ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
				for(Thread t : threadSet)
				{
					if(thread != null) {
						for(int i = 0; i < thread.length; ++i)
						{
							if(t.getId() == thread[i])
							{
								System.out.print("DEADLOCK: ");
							}
						}
					}

					System.out.println(t.getName() + " " + t.getState());
				}
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		});
		monitoringThread.setDaemon(true);
		monitoringThread.start();
	}
	
	private static boolean initProp()
	{
		//CONFIG 로드 부분
		try
		{
			InputStream stream = FileHandler.getExtInputStream("/config.properties");
            
			properties.load(stream);
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "config 로드 실패", e);
			return false;
		}
		logger.log(Level.INFO, "config 로드");
		return true;
	}
	
	private static boolean initJNIlib()
	{
		//JNI링크 부분
		File lib = FileHandler.getExtResourceFile("lib");
		File extlib = FileHandler.getExtResourceFile("extlib");
		logger.log(Level.INFO, lib.getAbsolutePath());
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(lib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.library.path"));
		System.setProperty("java.library.path", libPathBuffer.toString());
		logger.log(Level.INFO, libPathBuffer.toString());
		
		libPathBuffer = new StringBuffer();
		libPathBuffer.append(extlib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.ext.dirs"));
		System.setProperty("java.ext.dirs", libPathBuffer.toString());
		logger.log(Level.INFO, libPathBuffer.toString());
		
		Field sysPathsField = null;
		try
		{
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			// TODO Auto-generated catch blsock
			logger.log(Level.SEVERE, "JNI 라이브러리 폴더 링크 실패", e1);
			return false;
		}
		logger.log(Level.INFO, "JNI 라이브러리 로드");
		return true;
	}

	private DB_Handler dbHandler;
	private ProtocolSerial protocolSerial;
	//private SerialReadManager serialSensorReadManager;
	private TcpSensorReadManager tcpSensorReadManager;
	private SensorManager sensorManager;
	private SensorDataInUSBManager sensorDataInUSBManager;
	private WebManager webManager;
	private ClientSessionManager clientSessionManager;
	private DisplayDeviceState displayDeviceState;
	private AppServiceManager appServiceManager;
	//private AccelSafetyControlManager accelSafetyControl;
	private O2SafetyControlManager o2SafetyControl;
	private TestVirtualSensorManager testSensor;

	private ServerCore()
	{
		this.dbHandler = new DB_Handler();
		this.protocolSerial = new ProtocolSerial();
		//this.serialSensorReadManager = new SerialReadManager();
		//this.tcpSensorReadManager = new TcpSensorReadManager();
		this.sensorManager = new SensorManager(this.protocolSerial);
		this.sensorDataInUSBManager = new SensorDataInUSBManager(this.sensorManager);
		this.webManager = new WebManager();
		this.clientSessionManager = new ClientSessionManager();
		this.clientSessionManager.addSessionProvider(this.webManager.webSessionManager);
		this.appServiceManager = new AppServiceManager(this.clientSessionManager, this.sensorManager);
		//this.appServiceManager = new O2AppServiceManager(this.clientSessionManager, this.sensorManager);
		this.displayDeviceState = new DisplayDeviceState(this.sensorManager, this.clientSessionManager);
		//this.testSensor = new TestVirtualSensorManager(this.sensorManager);
		//this.accelSafetyControl = new AccelSafetyControlManager(this.sensorManager, this.protocolSerial);
		this.o2SafetyControl = new O2SafetyControlManager(this.sensorManager, this.protocolSerial);
		
	}

	private boolean start()
	{
		DisplayObject loadingText = DisplayControl.inst().showString(-1, -1, "DB모듈 로드중");
		if(!this.dbHandler.startModule()) return false;
		DB_Installer dbInstaller = new DB_Installer(this.dbHandler);
		loadingText = DisplayControl.inst().replaceString(loadingText, "시리얼 로드");
		if(!this.protocolSerial.startModule()) return false;
		//if(!this.serialSensorReadManager.startModule()) return false;
		//if(!this.tcpSensorReadManager.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "센서 매니저 로드");
		if(!this.sensorManager.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "USB저장기 로드");
		if(!this.sensorDataInUSBManager.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "웹 서비스 로드");
		if(!this.webManager.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "세션 관리자 로드");
		if(!this.clientSessionManager.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "사용자 서비스 로드");
		if(!this.appServiceManager.startModule()) return false;
		//this.testSensor.start();
		loadingText = DisplayControl.inst().replaceString(loadingText, "안전관리모듈 로드");
		//if(!this.accelSafetyControl.startModule()) return false;
		if(!this.o2SafetyControl.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "상태 표시모듈 로드");
		if(!this.displayDeviceState.startModule()) return false;
		loadingText = DisplayControl.inst().replaceString(loadingText, "시스템 시작 완료");
		DisplayControl.inst().removeShapeTimer(loadingText, 3000);
		logger.log(Level.INFO, "시스템 시작 완료");
		dbInstaller.complete();
		return true;
	}

	private void shutdown()
	{
		DisplayObject endText = DisplayControl.inst().showString(-1, -1, "시스템 종료중");
		logger.log(Level.INFO, "시스템 종료 시작");
		this.o2SafetyControl.stopModule();
		//this.accelSafetyControl.stopModule();
		this.displayDeviceState.stopModule();
		//this.testSensor.stop();
		this.appServiceManager.stopModule();
		this.clientSessionManager.stopModule();
		this.webManager.stopModule();
		this.sensorDataInUSBManager.stopModule();
		this.sensorManager.stopModule();
		//this.tcpSensorReadManager.stopModule();
		//this.serialSensorReadManager.stopModule();
		this.protocolSerial.stopModule();
		this.dbHandler.stopModule();
		logger.log(Level.INFO, "시스템 종료 완료");
		DisplayControl.inst().removeShape(endText);
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public static String getProp(String key)
	{
		String prop = properties.getProperty(key);
		if(prop == null)
		{
			logger.log(Level.SEVERE, "properties 로드 실패 key:"+key);
			return null;
		}
		
		return prop;
	}
}
