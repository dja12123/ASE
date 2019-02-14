package telco.sensorReadServer.sensorManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.console.LogWriter;
import telco.sensorReadServer.ServerCore;
import telco.sensorReadServer.db.DB_Handler;
import telco.sensorReadServer.db.DB_Installer;
import telco.sensorReadServer.serialReader.DevicePacket;
import telco.sensorReadServer.serialReader.SerialReadManager;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class SensorManager extends Observable<SensorStateChangeEvent> implements Observer<DevicePacket> 
{
	public static final Logger logger = LogWriter.createLogger(SensorManager.class, "sensorManager");
	public static final String PROP_SENSOR_TIMEOUT = "SensorTimeout";
	public static final String PROP_SENSOR_CHECK_INTERVAL = "SensorCheckInterval";
	private static final String DB_Schema = 
			"CREATE TABLE Device(" + 
			"id INTEGER," + 
			"lastUpdateTime TEXT," + 
			"PRIMARY KEY(`id`)" + 
			");";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	
	private SerialReadManager serialReader;
	private DB_Handler dbHandler;
	
	private boolean isRun;
	private Thread timeoutCheckThread;
	private HashMap<Integer, Sensor> sensorMap;
	private int sensorTimeout;
	private int checkInterval;
	
	public SensorManager(SerialReadManager serialReader, DB_Handler dbHandler)
	{
		this.serialReader = serialReader;
		this.dbHandler = dbHandler;
		this.sensorMap = new HashMap<Integer, Sensor>();
	}
	
	public boolean startModule(DB_Installer dbinit)
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		logger.log(Level.INFO, "SensorManager 시작");
		
		dbinit.checkAndCreateTable(DB_Schema);
		
		this.dbHandler.executeQuery("select * from Device;", this::queryAllDeviceCallback);
		
		this.sensorTimeout = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_TIMEOUT));
		this.checkInterval = Integer.parseInt(ServerCore.getProp(PROP_SENSOR_CHECK_INTERVAL));
		this.serialReader.addObserver(this);
		
		this.timeoutCheckThread = new Thread(this::timeoutCheck);
		this.timeoutCheckThread.setDaemon(true);
		this.timeoutCheckThread.start();
		
		logger.log(Level.INFO, "SensorManager 시작 완료");
		return true;
	}
	
	private void queryAllDeviceCallback(PreparedStatement prep)
	{
		try
		{
			ResultSet rs = prep.getResultSet();
			System.out.println(rs.getInt(1));
			while(rs.next())
			{
				int id = rs.getInt(1);
				Date updateDate = DATE_FORMAT.parse(rs.getString(2));
				Sensor sensor = new Sensor(id, updateDate);
				this.sensorMap.put(id,  sensor);
				logger.log(Level.INFO, "등록된 장치 로드:"+id);
			}
		}
		catch (SQLException | ParseException e)
		{
			logger.log(Level.SEVERE, "장치 조회 실패", e);
		}
		this.checkTimeoutTask();
	}
	
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		logger.log(Level.INFO, "SensorManager관리자 종료");
		this.timeoutCheckThread.interrupt();
		
		this.serialReader.removeObserver(this);
		
		for(Sensor s : this.sensorMap.values())
		{
			logger.log(Level.INFO, "장치 저장:"+s.id);
			if(this.dbHandler.hasResult("select id from Device where id="+s.id+";"))
			{
				StringBuffer query = new StringBuffer();
				query.append("insert into Device values(");
				query.append(s.id);
				query.append(", ");
				query.append(DATE_FORMAT.format(s.getLastUpdateTime()));
				query.append(");");
				this.dbHandler.executeQuery(query.toString());
			}
			else
			{
				StringBuffer query = new StringBuffer();
				query.append("update device set lastUpdateTime='");
				query.append(DATE_FORMAT.format(s.getLastUpdateTime()));
				query.append("' where id=");
				query.append(s.id);
				query.append(";");
				this.dbHandler.executeQuery(query.toString());
			}
		}
		logger.log(Level.INFO, "SensorManager 관리자 종료 완료");
		this.sensorMap.clear();
	}

	@Override
	public void update(Observable<DevicePacket> object, DevicePacket data)
	{
		Sensor s = this.sensorMap.getOrDefault(data.ID, null);
		if(s != null)
		{// 기존 장치 업데이트
			if(!s.isOnline)
			{
				logger.log(Level.INFO, "장치 온라인:"+s.id);
				s.isOnline = true;
				SensorStateChangeEvent e = new SensorStateChangeEvent(s, true);
				this.notifyObservers(e);
				s.alartDataReceive(data.X_GRADIANT, data.Y_GRADIANT, data.X_ACCEL, data.Y_ACCEL, data.Z_ACCEL, data.Altitiude);
			}
		}
		else
		{// 새 장치 접근
			s = new Sensor(data.ID);
			s.isOnline = true;
			logger.log(Level.INFO, "새 장치 접근:"+s.id);
			this.sensorMap.put(data.ID, s);
			
			SensorStateChangeEvent e = new SensorStateChangeEvent(s, true);
			this.notifyObservers(e);
			s.alartDataReceive(data.X_GRADIANT, data.Y_GRADIANT, data.X_ACCEL, data.Y_ACCEL, data.Z_ACCEL, data.Altitiude);
		}
	}
	
	private void checkTimeoutTask()
	{
		long compareTime = new Date().getTime() - this.sensorTimeout;
		
		for(Sensor sensor : this.sensorMap.values())
		{
			if(compareTime > sensor.getLastUpdateTime().getTime())
			{//타임아웃일때
				logger.log(Level.WARNING, "장치 타임아웃:"+sensor.id);
				SensorStateChangeEvent e = new SensorStateChangeEvent(sensor, false);
				this.notifyObservers(e);
			}
		}
	}
	
	public void registerDevice(int id)
	{
		if(!this.sensorMap.containsKey(id))
		{
			Sensor s = new Sensor(id);
			this.sensorMap.put(id, s);
		}
		else
		{
			throw new RuntimeException("이미 있는 장치");
		}
	}
	
	private void timeoutCheck()
	{
		while(this.isRun)
		{
			this.checkTimeoutTask();
			try
			{
				Thread.sleep(this.checkInterval);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
