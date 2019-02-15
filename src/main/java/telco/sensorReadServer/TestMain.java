package telco.sensorReadServer;

import java.util.logging.Logger;

import telco.console.LogWriter;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(TestMain.class, "main");// 메인 로거
	
	public static void main(String[] args) throws Exception
	{

		System.out.println("종료");
		Thread.sleep(2000);

	}

}
