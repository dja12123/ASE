package ase.web;

//
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.web.httpServer.HTTPServer;
import ase.web.webSocket.WebSessionManager;
import ase.web.webSocket.WebSocketHandler;

public class WebManager
{
	public static final String PROP_WEBSERVERPORT = "WebServerPort";
	public static final String PROP_WEBSOCKETPORT = "WebSocketPort";
	
	private static final Logger logger = LogWriter.createLogger(WebManager.class, "WebService");
	
	private final int webServerPort;
	private final int webSocketPort;

	private final HTTPServer httpServer;
	public final WebSocketHandler webSocketHandler;
	public final WebSessionManager webSessionManager;

	public WebManager()
	{
		this.webServerPort = Integer.parseInt(ServerCore.getProp(PROP_WEBSERVERPORT));
		this.webSocketPort = Integer.parseInt(ServerCore.getProp(PROP_WEBSOCKETPORT));
		
		this.webSocketHandler = new WebSocketHandler(this.webSocketPort);
		this.webSessionManager = new WebSessionManager(this.webSocketHandler.channelObservable);
		this.httpServer = new HTTPServer(this.webServerPort, this.webSessionManager);
		
		
	}

	public boolean startModule()
	{
		logger.log(Level.INFO, "웹 서비스 시작");
		try
		{
			this.webSocketHandler.start(-1);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "웹 소켓 시작중 오류", e);
			return false;
		}
		this.httpServer.startModule();
		this.webSessionManager.start();
		logger.log(Level.INFO, "웹 서비스 시작 완료");
		return true;
	}

	public void stopModule()
	{
		logger.log(Level.INFO, "웹 서비스 종료");
		this.webSessionManager.stop();
		this.webSocketHandler.stop();
		this.httpServer.stop();
		logger.log(Level.INFO, "웹 서비스 종료 완료");
	}

}
