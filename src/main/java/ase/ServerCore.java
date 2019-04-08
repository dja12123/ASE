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
import ase.hardware.DisplayControl;
import ase.hardware.DisplayObject;
import ase.hardware.GPIOControl;
import ase.sensorDataInUSB.SensorDataInUSBManager;
import ase.sensorManager.SensorManager;
import ase.sensorReader.SerialReadManager;
import ase.sensorReader.TcpSensorReadManager;
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
		shutdownThreads = getShutdownHookList();
		
		for(Thread beforeShutdownThread : getShutdownHookList())
		{
			Runtime.getRuntime().removeShutdownHook(beforeShutdownThread);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(ServerCore::endProgram, "shutdownThread"));
		
		if(!initProp())
		{
			return;
		}
		
		DisplayControl.init();
		GPIOControl.init();
		mainThread = Thread.currentThread();
		mainInst = new ServerCore();
		
		Thread shutdownThread = new Thread(ServerCore::endProgram, "shutdownThread");
		shutdownThread.setPriority(Thread.MAX_PRIORITY);
		
		if (!mainInst.start())
		{
			logger.log(Level.SEVERE, "초기화 실패");
			return;
		}
		while(true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
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
			e.printStackTrace();
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
	private SerialReadManager serialSensorReadManager;
	private TcpSensorReadManager tcpSensorReadManager;
	private SensorManager sensorManager;
	private SensorDataInUSBManager sensorDataInUSBManager;
	private WebManager webManager;
	private ClientSessionManager clientSessionManager;
	
	private AppServiceManager appServiceManager;
	
	private TestVirtualSensorManager testSensor;

	private ServerCore()
	{
		this.dbHandler = new DB_Handler();
		this.serialSensorReadManager = new SerialReadManager();
		//this.tcpSensorReadManager = new TcpSensorReadManager();
		this.sensorManager = new SensorManager(this.dbHandler, this.serialSensorReadManager);
		this.sensorDataInUSBManager = new SensorDataInUSBManager();
		this.webManager = new WebManager();
		this.clientSessionManager = new ClientSessionManager();
		this.clientSessionManager.addSessionProvider(this.webManager.webSessionManager);
		this.appServiceManager = new AppServiceManager(this.clientSessionManager, this.sensorManager);
		
		//this.testSensor = new TestVirtualSensorManager(this.sensorManager);
	}

	private boolean start()
	{
		DisplayObject loadingText = DisplayControl.inst().showString(-1, -1, "DB모듈 로드중");
		if(!this.dbHandler.startModule()) return false;
		DB_Installer dbInstaller = new DB_Installer(this.dbHandler);
		DisplayControl.inst().replaceString(loadingText, "센서 serial 로드");
		if(!this.serialSensorReadManager.startModule()) return false;
		//if(!this.tcpSensorReadManager.startModule()) return false;
		DisplayControl.inst().replaceString(loadingText, "센서 매니저 로드");
		if(!this.sensorManager.startModule(dbInstaller)) return false;
		DisplayControl.inst().replaceString(loadingText, "USB 저장모듈 로드");
		if(!this.sensorDataInUSBManager.startModule()) return false;
		DisplayControl.inst().replaceString(loadingText, "웹 서비스 로드");
		if(!this.webManager.startModule()) return false;
		DisplayControl.inst().replaceString(loadingText, "세션 관리자 로드");
		if(!this.clientSessionManager.startModule()) return false;
		DisplayControl.inst().replaceString(loadingText, "사용자 서비스 로드");
		if(!this.appServiceManager.startModule()) return false;
		dbInstaller.complete();
		
		//this.testSensor.start();
		DisplayControl.inst().replaceString(loadingText, "시스템 시작 완료");
		DisplayControl.inst().removeShapeTimer(loadingText, 3000);
		logger.log(Level.INFO, "시스템 시작 완료");
		return true;
	}

	private void shutdown()
	{
	
		logger.log(Level.INFO, "시스템 종료 시작");
		
		//this.testSensor.stop();
		this.appServiceManager.stopModule();
		this.clientSessionManager.stopModule();
		this.webManager.stopModule();
		this.sensorDataInUSBManager.stopModule();
		this.sensorManager.stopModule();
		//this.tcpSensorReadManager.stopModule();
		this.serialSensorReadManager.stopModule();
		this.dbHandler.stopModule();
		logger.log(Level.INFO, "시스템 종료 완료");
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
