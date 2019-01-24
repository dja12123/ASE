package telco.sensorReadServer;

import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.comm.CommPortIdentifier;

import telco.sensorReadServer.appConnect.AppConnectManager;
import telco.sensorReadServer.console.LogWriter;
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
