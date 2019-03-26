package ase.sensorReadServer.sensorReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

import ase.appConnect.channel.ProtocolDefine;
import ase.console.LogWriter;
import ase.sensorReadServer.ServerCore;
import ase.util.BinUtil;
import ase.util.observer.Observable;

public class SerialReadManager extends Observable<DevicePacket>
{
	public static final String PROP_SerialDevice = "SerialDevice";
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	public static final int FULL_PACKET_SIZE = 32;
	public static final Logger logger = LogWriter.createLogger(SerialReadManager.class, "serialSensorReader");
	private static final byte[] SERIAL_STX = new byte[] {0x55, 0x77};
	
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
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e1){}
		try
		{
			this.serial.write(SERIAL_STX);
		}
		catch (IllegalStateException | IOException e)
		{
			logger.log(Level.SEVERE, "시리얼 STX전송중 오류", e);
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
		
		if(!this.isDevicePacket(receiveData))
		{
			logger.log(Level.WARNING, "오류! 센서 패킷이 아님" + BinUtil.bytesToHex(receiveData));
			return;
		}
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(receiveData);
		byteBuffer.order(BYTE_ORDER);
		
		int id = byteBuffer.getInt();
		int nsize = byteBuffer.getInt();
		float xg = byteBuffer.getFloat();
		float yg = byteBuffer.getFloat();
		float xa= byteBuffer.getFloat();
		float ya = byteBuffer.getFloat();
		float za = byteBuffer.getFloat();
		float al = byteBuffer.getFloat();
		
		DevicePacket packet = new DevicePacket(id, xg, yg, xa, ya, za, al, -1);
		
		this.notifyObservers(ServerCore.mainThreadPool, packet);
	}
	
	public boolean isDevicePacket(byte[] packet)
	{
		if(packet.length != FULL_PACKET_SIZE)
		{
			return false;
		}
		ByteBuffer buffer = ByteBuffer.wrap(packet);
		buffer.position(4);
		buffer.order(BYTE_ORDER);
		int packetSize = buffer.getInt();
		if(packetSize != FULL_PACKET_SIZE)
		{
			return false;
		}
		return true;
	}
}
