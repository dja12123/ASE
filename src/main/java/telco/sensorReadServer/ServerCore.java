package telco.sensorReadServer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.comm.CommPortIdentifier;

import telco.sensorReadServer.appConnect.AppConnectManager;
import telco.sensorReadServer.console.LogWriter;
import telco.sensorReadServer.fileIO.FileHandler;
import telco.sensorReadServer.sensorReader.SensorReadManager;
import telco.sensorReadServer.sensorReader.SerialReader;

public class ServerCore
{
	public static final Logger logger = LogWriter.createLogger(ServerCore.class, "main");// 메인 로거
	public static final ExecutorService mainThreadPool = Executors.newCachedThreadPool();

	private static ServerCore mainInst;
	
	
	static CommPortIdentifier portId;
	static Enumeration portList;
	public static void main(String[] args)
	{
		if(!initJNI())
		{
			logger.log(Level.SEVERE, "JNI링크 실패");
			return;
		}
		boolean portFound = false;
		String defaultPort = "/dev/ttyACM0";

		if (args.length > 0)
		{
			defaultPort = args[0];
		}

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements())
		{
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
			{
				if (portId.getName().equals(defaultPort))
				{
					System.out.println("Found port: " + defaultPort);
					portFound = true;
					SerialReader reader = new SerialReader();
				}
			}
		}
		if (!portFound)
		{
			System.out.println("port " + defaultPort + " not found.");
		}

	}
	
	/*public static void main(String[] args)
	{
		
		mainInst = new ServerCore();
		Runtime.getRuntime().addShutdownHook(new Thread(ServerCore::endProgram));
		if(!initJNI())
		{
			logger.log(Level.SEVERE, "JNI링크 실패");
			return;
		}
		
		if (!mainInst.start())
		{
			logger.log(Level.SEVERE, "초기화 실패");
			return;
		}
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}*/
	
	private static boolean initJNI()
	{
		//JNI링크 부분
		File rawlib = FileHandler.getExtResourceFile("native");
		logger.log(Level.INFO, rawlib.getAbsolutePath());
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(rawlib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.library.path"));
		
		System.setProperty("java.library.path", libPathBuffer.toString());
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
		this.appConnectManager.startModule();
		this.sensorReadManager.startModule();
		logger.log(Level.INFO, "시스템 시작 완료");
		return true;
	}

	private void shutdown()
	{

		this.appConnectManager.stopModule();
		this.sensorReadManager.stopModule();
		logger.log(Level.INFO, "시스템 종료 완료");
	}
}
