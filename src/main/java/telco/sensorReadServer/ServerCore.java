package telco.sensorReadServer;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.sensorReadServer.appConnect.AppConnectManager;
import telco.sensorReadServer.console.LogWriter;
import telco.sensorReadServer.fileIO.FileHandler;
import telco.sensorReadServer.sensorReader.SensorReadManager;

public class ServerCore
{
	private static final Properties properties = new Properties();
	public static final Logger logger = LogWriter.createLogger(ServerCore.class, "main");// 메인 로거
	public static final ExecutorService mainThreadPool = Executors.newCachedThreadPool();

	private static Thread mainThread;
	private static ServerCore mainInst;
	
	public static void main(String[] args)
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		monitoringThread.setDaemon(true);
		monitoringThread.start();
		
		mainThread = Thread.currentThread();
		mainInst = new ServerCore();
		
		if(!initProp())
		{
			return;
		}
		Thread shutdownThread = new Thread(ServerCore::endProgram, "shutdownThread");
		shutdownThread.setPriority(Thread.MAX_PRIORITY);
		Runtime.getRuntime().addShutdownHook(new Thread(ServerCore::endProgram, "shutdownThread"));
		
		if (!mainInst.start())
		{
			logger.log(Level.SEVERE, "초기화 실패");
			mainInst.shutdown();
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

	public static void endProgram()
	{
		mainInst.shutdown();
	}

	private AppConnectManager appConnectManager;
	private SensorReadManager sensorReadManager;

	private ServerCore()
	{
		this.appConnectManager = new AppConnectManager();
		this.sensorReadManager = new SensorReadManager();
	}

	private boolean start()
	{
		if(!this.appConnectManager.startModule()) return false;
		if(!this.sensorReadManager.startModule()) return false;
		logger.log(Level.INFO, "시스템 시작 완료");
		return true;
	}

	private void shutdown()
	{
		synchronized (Logger.getGlobal())
		{
			logger.log(Level.INFO, "시스템 종료 시작");
			this.appConnectManager.stopModule();
			this.sensorReadManager.stopModule();
			logger.log(Level.INFO, "시스템 종료 완료");
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.log(Level.INFO, "시스템 종료 완료");
		}
	}
	
	public static String getProp(String key)
	{
		return properties.getProperty(key);
	}
}
