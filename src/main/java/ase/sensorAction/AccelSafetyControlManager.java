package ase.sensorAction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorComm.ISensorCommManager;
import ase.sensorComm.ISensorTransmitter;
import ase.sensorComm.ProtoDef;
import ase.sensorManager.SensorManager;
import ase.sensorManager.accelSensorDataAnalyser.AccelSensorDataAnalyser;
import ase.sensorManager.accelSensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.accelSensorDataAnalyser.SafetyStatus;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorControl.SensorControlInterface;
import ase.util.observer.Observer;

public class AccelSafetyControlManager
{
	public static final Logger logger = LogWriter.createLogger(AccelSafetyControlManager.class, "AccelSafetyControlManager");
	public static final String PROP_ALERT_DELAY = "alertDelay";
	private boolean isRun;
	private final SensorManager sensorManager;
	private final SensorControlInterface sensorControl;
	private final AccelSensorDataAnalyser accelDataAnalyser;
	private ISensorCommManager commManager;
	private final Observer<SafeStateChangeEvent> safeObserver;
	private int alertDelay;
	
	private Timer timer;
	private TimerTask timerTask;
	private List<Sensor> speakList;
	
	public AccelSafetyControlManager(SensorManager sensorManager, ISensorCommManager commManager)
	{
		this.sensorManager = sensorManager;
		this.sensorControl = this.sensorManager.sensorControl;
		this.accelDataAnalyser = this.sensorManager.accelSensorDataAnalyser;
		this.commManager = commManager;
		this.safeObserver = this::safeObserver;
		this.speakList = new ArrayList<>();
		this.isRun = true;
	}
	
	public synchronized boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		this.alertDelay = Integer.valueOf(ServerCore.getProp(PROP_ALERT_DELAY));
		this.accelDataAnalyser.addObserver(this.safeObserver);
		logger.log(Level.INFO, "가속도 안전제어 모듈 활성화");
		return true;
	}
	
	public synchronized void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.accelDataAnalyser.removeObserver(this.safeObserver);
		if(this.timer != null)
		{
			this.timer.cancel();
		}
		this.speakList.clear();
		logger.log(Level.INFO, "가속도 안전제어 모듈 비활성화");
	}
	
	private synchronized void safeObserver(SafeStateChangeEvent event)
	{
		logger.log(Level.INFO, "안전상태 옵저버 작동");
		if(event.status == SafetyStatus.Safe)
		{
			this.speakList.remove(event.sensor);
			if(this.speakList.isEmpty())
			{
				this.timer.cancel();
			}
		}
		else if(event.status == SafetyStatus.Danger)
		{
			this.speakList.add(event.sensor);
			if(this.speakList.size() == 1)
			{
				this.timerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						AccelSafetyControlManager.this.sound();
					}
				};
				this.timer.schedule(this.timerTask, 1000, this.alertDelay);
			}
			
		}
		
		ISensorTransmitter transmitter = this.commManager.getUserMap().getOrDefault(event.sensor.ID, null);
		if(transmitter != null)
		{
			transmitter.putSegment(ProtoDef.KEY_S2C_SET_SAFETY_STATE, event.status.code);
		}
		
	}
	
	private void sound()
	{
		logger.log(Level.INFO, "경고음 알람 "+this.speakList.size()+"개");
		for(Sensor s : this.speakList)
		{
			ISensorTransmitter transmitter = this.commManager.getBroadcast();
			ByteBuffer buf = ByteBuffer.allocate(4);
			buf.put((byte)'A');
			buf.putShort((short) s.ID);
			buf.put(SafetyStatus.Danger.code);
			transmitter.putSegment(ProtoDef.KEY_S2C_PLAY_ALERT_SOUND, buf.array());
		}
	}
}
