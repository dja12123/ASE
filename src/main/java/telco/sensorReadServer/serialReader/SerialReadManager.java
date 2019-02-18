package telco.sensorReadServer.serialReader;

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

import telco.console.LogWriter;
import telco.sensorReadServer.ServerCore;
import telco.util.observer.Observable;

public class SerialReadManager extends Observable<DevicePacket>
{
	public static final String PROP_SerialDevice = "SerialDevice";
	
	public static final Logger logger = LogWriter.createLogger(SerialReadManager.class, "sensorReader");
	
	private Serial serial;
	private SerialConfig config;
	
	public SerialReadManager()
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
		logger.log(Level.INFO, "SerialReadManager 시작");
		this.config.device(ServerCore.getProp(PROP_SerialDevice));
		try
		{
			this.serial.open(this.config);
			
			logger.log(Level.INFO, "연결: " + this.config.toString());
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "시리얼 열기 실패", e);
			return false;
		}
		logger.log(Level.INFO, "SerialReadManager 시작 완료");
		return true;
	}
	
	public void stopModule()
	{
		logger.log(Level.INFO, "SerialReadManager 종료");
	}
	
	private void dataReceived(SerialDataEvent event)
	{
		byte[] receiveData;
		try
		{
			receiveData = event.getBytes();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		if(!DevicePacket.isDevicePacket(receiveData))
		{
			return;
		}
		
		DevicePacket packet = new DevicePacket(receiveData);
		this.notifyObservers(packet);
	}
}
