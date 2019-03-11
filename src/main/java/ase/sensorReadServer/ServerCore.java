package ase.sensorReadServer;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
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

import ase.console.LogWriter;
import ase.sensorReadServer.appService.AppServiceManager;
import ase.sensorReadServer.db.DB_Handler;
import ase.sensorReadServer.db.DB_Installer;
import ase.sensorReadServer.fileIO.FileHandler;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.serialReader.SerialReadManager;
import ase.sensorReadServer.serverSocket.ServerSocketManager;
import ase.test.TestVirtualSensorManager;

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
		
		mainThread = Thread.currentThread();
		mainInst = new ServerCore();
		
		if(!initProp())
		{
			return;
		}
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
	}
	
	public static void endProgram()
	{
		mainInst.shutdown();
		
		for(Thread nextShutdown : shutdownThreads)
		{
			nextShutdown.start();
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
			InputStream stream = FileHandler.getResourceAsStream("/config.properties");
            
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
	private ServerSocketManager serverSocketManager;
	private SerialReadManager sensorReadManager;
	private SensorManager sensorManager;
	private AppServiceManager appServiceManager;
	
	private TestVirtualSensorManager testSensor;

	private ServerCore()
	{
		this.dbHandler = new DB_Handler();
		this.serverSocketManager = new ServerSocketManager();
		this.sensorReadManager = new SerialReadManager();
		this.sensorManager = new SensorManager(this.sensorReadManager, this.dbHandler);
		this.appServiceManager = new AppServiceManager(this.serverSocketManager, this.sensorManager);
		
		this.testSensor = new TestVirtualSensorManager(this.sensorManager);
	}

	private boolean start()
	{
		if(!this.dbHandler.startModule()) return false;
		DB_Installer dbInstaller = new DB_Installer(this.dbHandler);
		if(!this.serverSocketManager.startModule()) return false;
		if(!this.sensorReadManager.startModule()) return false;
		if(!this.sensorManager.startModule(dbInstaller)) return false;
		if(!this.appServiceManager.startModule()) return false;
		dbInstaller.complete();
		
		//this.testSensor.start();
		
		logger.log(Level.INFO, "시스템 시작 완료");
		return true;
	}

	private void shutdown()
	{
	
		logger.log(Level.INFO, "시스템 종료 시작");
		
		//this.testSensor.stop();
		
		this.appServiceManager.stopModule();
		this.serverSocketManager.stopModule();
		this.sensorManager.stopModule();
		this.sensorReadManager.stopModule();
		this.dbHandler.stopModule();
		logger.log(Level.INFO, "시스템 종료 완료");
	}
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
}
