package ase.sensorManager.o2SensorDataAnalyser;

import java.util.logging.Level;
import java.util.logging.Logger;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.sensorManager.AbsSensorEventStateManager;
import ase.sensorManager.SensorManager;
import ase.sensorManager.alias.SensorAliasManager;
import ase.sensorManager.sensor.Sensor;
import ase.sensorManager.sensorDataO2.O2DataReceiveEvent;
import ase.sensorManager.sensorDataO2.SensorO2Data;
import ase.sensorManager.sensorDataO2.SensorO2DataManager;
import ase.sensorManager.sensorLog.SensorLogManager;
import ase.util.observer.Observer;

public class O2SensorDataAnalyseManager extends AbsSensorEventStateManager<SafeStateChangeEvent, SafetyStatus>
{
	public static final Logger logger = LogWriter.createLogger(O2SensorDataAnalyseManager.class, "O2SensorDataAnalyser");

	public static final float SAFE_THRESHOLD = 0.20F;
	public static final float WARNING_THRESHOLD = 0.18F;
	
	private final Observer<O2DataReceiveEvent> o2DataObserver;
	private final SensorO2DataManager dataManager;
	private final SensorLogManager sensorLogManager;
	
	public O2SensorDataAnalyseManager(SensorManager sensorManager, SensorO2DataManager dataManager, SensorLogManager sensorLogManager)
	{
		super(sensorManager);
		this.o2DataObserver = this::o2DataObserver;
		this.dataManager = dataManager;
		this.sensorLogManager = sensorLogManager;
	}
	
	private void o2DataObserver(O2DataReceiveEvent e)
	{
		SafetyStatus beforeStatus = this.state.getOrDefault(e.sensorInst, null);
		if(beforeStatus != null)
		{
			SafetyStatus nowStatus = this.checkSafe(e.data);
			if(beforeStatus != nowStatus)
			{
				SafeStateChangeEvent event = new SafeStateChangeEvent(e.sensorInst, nowStatus);
				this.changeState(e.sensorInst, nowStatus);
				this.provideEvent(ServerCore.mainThreadPool, e.sensorInst, event);
				this.sensorLogManager.appendLog(e.sensorInst, Level.INFO, "Safety State Change ("+beforeStatus+"->"+nowStatus+")");
				logger.log(Level.INFO, e.sensorInst.ID + " 산소레벨 상태변경" + nowStatus);
			}
		}
	}
	
	private SafetyStatus checkSafe(SensorO2Data data)
	{
		if(data.value >= SAFE_THRESHOLD)
		{//안전
			return SafetyStatus.Safe;
		}
		else if(data.value >= WARNING_THRESHOLD)
		{//주의
			return SafetyStatus.Warning;
		}
		else
		{//위험
			return SafetyStatus.Danger;
		}
	}

	@Override
	protected void onStart()
	{
		this.dataManager.addObserver(this.o2DataObserver);
	}

	@Override
	protected void onStop()
	{
		this.dataManager.removeObserver(this.o2DataObserver);
	}

	@Override
	protected SafetyStatus onRegisterSensor(Sensor sensor)
	{
		SensorO2Data o2Data = this.dataManager.getLastSensorData(sensor);
		if(o2Data != null)
		{
			return this.checkSafe(o2Data);
		}
		return SafetyStatus.Safe;
	}

	@Override
	protected void onRemoveSensor(Sensor sensor)
	{
		
	}

}
