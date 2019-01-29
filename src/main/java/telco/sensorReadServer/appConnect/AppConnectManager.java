package telco.sensorReadServer.appConnect;

import java.util.logging.Level;
import java.util.logging.Logger;

import telco.sensorReadServer.console.LogWriter;

public class AppConnectManager
{
	public static final Logger logger = LogWriter.createLogger(AppConnectManager.class, "appConnect");
	
	public AppConnectManager()
	{
		
	}
	
	public boolean startModule()
	{
		logger.log(Level.SEVERE, "AppConnectManager 시작");
		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.SEVERE, "AppConnectManager 종료");
	}
}
