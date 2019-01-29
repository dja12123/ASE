package telco.sensorReadServer.sensorReader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.console.LogWriter;

public class SensorReadManager
{
	public static final String PROP_SerialDevice = "SerialDevice";
	
	public static final Logger logger = LogWriter.createLogger(SensorReadManager.class, "sensorReader");
	
	private Serial serial;
	private SerialConfig config;
	
	public SensorReadManager()
	{
		this.serial = SerialFactory.createInstance();
		
		this.serial.addListener(this::dataReceived);
	
		this.config = new SerialConfig();

		this.config
		.baud(Baud._115200)
		.dataBits(DataBits._8)
		.parity(Parity.NONE)
		.stopBits(StopBits._1)
		.flowControl(FlowControl.NONE);
	}
	
	public boolean startModule()
	{
		this.config.device(ServerCore.getProp(PROP_SerialDevice));
		try
		{
			this.serial.open(this.config);
			logger.log(Level.INFO, "연결: " + config.toString());
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "시리얼 열기 실패", e);
			return false;
		}
		logger.log(Level.INFO, "SerialReadManager 시작");
		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "SerialReadManager 종료");
	}
	
	private void dataReceived(SerialDataEvent event)
	{
		try
		{
			logger.log(Level.INFO, event.getHexByteString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
