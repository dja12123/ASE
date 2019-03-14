package ase.web;
//
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;

public class WebManager {
	private static final Logger logger = LogWriter.createLogger(WebManager.class, "WebService");
	private final HTTPServer httpServer;
	public final WebSocketHandler webSocketHandler;
	// 옵저버 관련된 코드 이곳에 모두 추가
	
	
	public WebManager() {
		this.httpServer = new HTTPServer();
		this.webSocketHandler = new WebSocketHandler(8080, true);
	}
	
	public static void main(String[] args) {
		WebManager webServer = new WebManager();
		
		webServer.startModule();
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
			logger.log(Level.SEVERE, "웹 서비스 시작중 오류", e);
			return false;
		}
		this.httpServer.start();
		return true;
	}

	public void stopModule()
	{
		this.webSocketHandler.stop();
		this.httpServer.stop();
		
	}

}
