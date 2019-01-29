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
	
	public SensorReadManager()
	{

	}
	
	public boolean startModule()
	{
		
		this.serial = SerialFactory.createInstance();
		
		this.serial.addListener(this::dataReceived);
		

		try
		{
			SerialConfig config = new SerialConfig();

			config
			.device(ServerCore.getProp(PROP_SerialDevice))
			.baud(Baud._115200)
			.dataBits(DataBits._8)
			.parity(Parity.NONE)
			.stopBits(StopBits._1)
			.flowControl(FlowControl.NONE);

			logger.log(Level.INFO, "연결: " + config.toString());

			this.serial.open(config);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "시리얼 열기 실패", e);
			return false;
		}
		logger.log(Level.SEVERE, "SerialReadManager 시작");
		return true;
	}
	
	public void stopModule()
	{
		synchronized (this.serial)
		{
			if(this.serial.isOpen())
			{
				try
				{
					this.serial.discardData();
					this.serial.close();
				}
				catch (IllegalStateException e)
				{
					logger.log(Level.SEVERE, "시리얼 닫기 실패", e);
				}
				catch(IOException e)
				{
					logger.log(Level.SEVERE, "시리얼 종료");
				}
			}
		}
		logger.log(Level.SEVERE, "SerialReadManager 종료");
	}
	
	private void dataReceived(SerialDataEvent event)
	{
		try
		{
			logger.log(Level.INFO, event.getHexByteString());
			event.discardData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
