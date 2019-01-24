package telco.sensorReadServer.sensorReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import telco.sensorReadServer.console.LogWriter;

public class SerialReader
{
	public static final String SERIAL_PORT_NAME = "/dev/ttyACM0";
	private static final Logger logger = LogWriter.createLogger(SerialReader.class, "serialReader");
	
	private CommPortIdentifier portIdentifier;
	
	public SerialReader()
	{
		CommPortIdentifier portIdentifier;
		try
		{
			this.portIdentifier = CommPortIdentifier.getPortIdentifier(SERIAL_PORT_NAME);
		}
		catch (NoSuchPortException e)
		{
			logger.log(Level.SEVERE, "시리얼 읽기 실패", e);
			return;
		}
		System.out.println(this.portIdentifier);
	}
}
