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
import ase.sensorManager.alias.SensorAliasManager;
import ase.sensorManager.o2SensorDataAnalyser.SafeStateChangeEvent;
import ase.sensorManager.o2SensorDataAnalyser.SafetyStatus;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.o2SensorDataAnalyser.O2SensorDataAnalyseManager;
import ase.sensorManager.sensorControl.SensorControlInterface;
import ase.util.observer.Observer;

public class O2SafetyControlManager
{
	public static final Logger logger = LogWriter.createLogger(O2SafetyControlManager.class, "O2SafetyControlManager");
	public static final String PROP_ALERT_DELAY = "alertDelay";
	
	private final SensorManager sensorManager;
	private final SensorControlInterface sensorControl;
	private final O2SensorDataAnalyseManager o2DataAnalyser;
	private final ISensorCommManager commManager;
	private final SensorAliasManager sensorAliasManager;
	
	private int alertDelay;
	private boolean isRun;
	private Timer timer;
	private TimerTask timerTask;
	private List<Sensor> speakList;
	private Observer<SafeStateChangeEvent> safeObserver;
	
	public O2SafetyControlManager(SensorManager sensorManager, ISensorCommManager commManager)
	{
		this.sensorManager = sensorManager;
		this.sensorControl = this.sensorManager.sensorControl;
		this.o2DataAnalyser = this.sensorManager.o2SensorDataAnalyser;
		this.sensorAliasManager = this.sensorManager.sensorAliasManager;
		this.safeObserver = this::safeObserver;
		this.commManager = commManager;
		
		this.speakList = new ArrayList<>();
		this.isRun = false;
	}
	
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		this.alertDelay = Integer.valueOf(ServerCore.getProp(PROP_ALERT_DELAY));
		this.o2DataAnalyser.addObserver(this.safeObserver);
		logger.log(Level.INFO, "산소 안전제어 모듈 활성화");
		return true;
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		this.o2DataAnalyser.removeObserver(this.safeObserver);
		if(this.timer != null)
		{
			this.timer.cancel();
		}
		this.speakList.clear();
		logger.log(Level.INFO, "산소 안전제어 모듈 비활성화");
	}
	
	private synchronized void safeObserver(SafeStateChangeEvent event)
	{
		if(event.status == SafetyStatus.Safe || event.status == SafetyStatus.Warning)
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
				this.timer = new Timer();
				this.timerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						O2SafetyControlManager.this.sound();
					}
				};
				this.timer.schedule(this.timerTask, 1000, this.alertDelay);
			}
			
		}
		
		ISensorTransmitter transmitter = this.commManager.getUserMap().getOrDefault(event.sensor.ID, null);
		if(transmitter != null)
		{
			byte[] payload = new byte[1];
			payload[0] = event.status.code;
			transmitter.putSegment(ProtoDef.KEY_S2C_SET_SAFETY_STATE, payload);
		}
	}
	
	private void sound()
	{
		logger.log(Level.INFO, "경고음 알람 "+this.speakList.size()+"개");
		for(Sensor s : this.speakList)
		{
			ISensorTransmitter transmitter = this.commManager.getBroadcast();
			ByteBuffer buf = ByteBuffer.allocate(4);
			
			String alias = this.sensorAliasManager.state.getOrDefault(s, null);
			if(alias == null) return;
			buf.put((byte)alias.charAt(0));
			if(alias.length() > 1)
			{
				short num = Short.valueOf(alias.substring(1, alias.length()));
				buf.putShort(num);
			}
			else
			{
				buf.putShort((short) -1);
			}
			
			buf.put(SafetyStatus.Danger.code);
			transmitter.putSegment(ProtoDef.KEY_S2C_PLAY_ALERT_SOUND, buf.array());
		}
	}
	
}
