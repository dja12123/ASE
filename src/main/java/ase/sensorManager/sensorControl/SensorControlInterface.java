package ase.sensorManager.sensorControl;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ISensorTransmitter;
import ase.sensorComm.ProtoDef;
import ase.sensorComm.protocolSerial.ProtocolSerial;
import ase.sensorComm.protocolSerial.SerialProtoDef;
import ase.sensorComm.protocolSerial.SerialTransmitter;
import ase.sensorManager.AbsCommSensorStateManager;
import ase.sensorManager.AbsSensorStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.o2SensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.o2SensorDataAnalyser.SafetyStatus;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.util.BinUtil;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorControlInterface extends Observable<SensorControlEvent>
{
	public static final Logger logger = LogWriter.createLogger(SensorControlInterface.class, "SensorControlInterface");
	private final SensorManager sensorManager;
	private final ISensorCommManager sensorComm;
	private final SensorLogManager sensorLogManager;
	
	public SensorControlInterface(SensorManager sensorManager, ISensorCommManager sensorComm, SensorLogManager sensorLogManager)
	{
		this.sensorManager = sensorManager;
		this.sensorComm = sensorComm;
		this.sensorLogManager = sensorLogManager;
	}
	
	public synchronized void startModule()
	{
		logger.log(Level.INFO, "센서 제어기 활성화");
	}
	
	public synchronized void stopModule()
	{
		this.clearObservers();
		logger.log(Level.INFO, "센서 제어기 비활성화");
	}

	public void playSound(Sensor sensor, short[] soundKey)
	{
		if(!this.sensorManager.sensorMap.containsKey(sensor.ID)) return;
		ISensorTransmitter transmitter = this.sensorComm.getUserMap().getOrDefault(sensor.ID, null);
		if(transmitter == null) return;
		ByteBuffer buffer = ByteBuffer.allocate(soundKey.length * Short.BYTES);
		for(short key : soundKey)
		{
			buffer.putShort(key);
		}
		transmitter.putSegment(ProtoDef.KEY_S2C_PLAY_SOUND, buffer.array());
		this.sensorLogManager.appendLog(sensor, Level.INFO, "Play Sound");
		logger.log(Level.INFO, sensor.ID + " Play Sound");
	}
	
	public void toggleSW(Sensor sensor, byte outputNo, boolean isOn)
	{
		if(!this.sensorManager.sensorMap.containsKey(sensor.ID)) return;
		ISensorTransmitter transmitter = this.sensorComm.getUserMap().getOrDefault(sensor.ID, null);
		if(transmitter == null) return;
		byte[] buffer = new byte[1+1];
		buffer[0] = outputNo;
		buffer[1] = (byte) (isOn?1:0);
		transmitter.putSegment(ProtoDef.KEY_S2C_SW_CONTROL, buffer);
		this.sensorLogManager.appendLog(sensor, Level.INFO, "Toggle Swtich No:"+outputNo+" State:"+(isOn?"ON":"OFF"));
		logger.log(Level.INFO, sensor.ID + " Toggle Swtich No:"+outputNo+" State:"+(isOn?"ON":"OFF"));
	}
	
}
