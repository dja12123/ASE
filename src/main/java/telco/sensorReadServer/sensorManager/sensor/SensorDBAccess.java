package telco.sensorReadServer.sensorManager.sensor;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

import telco.console.LogWriter;
import telco.sensorReadServer.db.DB_Handler;
import telco.sensorReadServer.db.DB_Installer;
import telco.util.observer.Observable;

public class SensorDBAccess
{
	// 센서의 DB접근을 제어합니다.
	private static final Logger logger = LogWriter.createLogger(SensorDBAccess.class, "sensorCreater");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

	private static final String SCHEMA_Sensor = 
			"CREATE TABLE Sensor(" + 
			"id INTEGER," + 
			"lastUpdateTime TEXT," +
			"online BOOL," +
			"PRIMARY KEY(`id`)" + 
			");";
	
	private static final String SCHEMA_Sensor_Log = 
			"CREATE TABLE Sensor_Log(" + 
			"id INTEGER," + 
			"time TEXT," +
			"logLevel TEXT," +
			"message TEXT," +
			"foreign key(id) references Sensor(id) on delete cascade" +
			");";
	
	private static final String SCHEMA_Sensor_Data = 
			"CREATE TABLE Sensor_Data(" + 
			"id INTEGER," + 
			"recordTime TEXT," +
			"x_gradiant FLOAT," +
			"y_gradiant FLOAT," +
			"x_accel FLOAT," +
			"y_accel FLOAT," +
			"z_accel FLOAT," +
			"altitiude FLOAT," +
			"foreign key(id) references Sensor(id) on delete cascade" +
			");";
	
	private DB_Handler dbHandler;
	
	public SensorDBAccess(DB_Handler dbHandler, DB_Installer dbInstaller)
	{
		this.dbHandler = dbHandler;
		dbInstaller.checkAndCreateTable(SCHEMA_Sensor);
		dbInstaller.checkAndCreateTable(SCHEMA_Sensor_Log);
		dbInstaller.checkAndCreateTable(SCHEMA_Sensor_Data);
	}
	
	public ArrayList<Sensor> getSensorFromDB(
			SensorConfigAccess config,
			Observable<DataReceiveEvent> publicDataReceiveObservable,
			Observable<SensorOnlineEvent> publicSensorOnlineObservable)
	{
		ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
		try
		{
			CachedRowSet rs = this.dbHandler.query("select * from Sensor;");
			while(rs.next())
			{
				int id = rs.getInt(1);
				Date updateDate = DATE_FORMAT.parse(rs.getString(2));
				Sensor sensor = new Sensor(id, rs.getBoolean(3), updateDate, this, config, publicDataReceiveObservable, publicSensorOnlineObservable);
				
				CachedRowSet sensorLogRS = this.dbHandler.query("select * from Sensor_Log where id="+id+";");
				while(sensorLogRS.next())
				{
					Date d = DATE_FORMAT.parse(sensorLogRS.getString(2));
					Level l = Level.parse(sensorLogRS.getString(3));
					String msg = sensorLogRS.getString(4);
					SensorLog log = new SensorLog(l, d, msg);
					sensor._log.add(log);
					if(sensor._log.size() >= config.getMaxLog())
					{
						break;
					}
				}
				
				CachedRowSet sensorDataRS = this.dbHandler.query("select * from Sensor_Data where id="+id+";");
				DB_Handler.printResultSet(sensorDataRS);
				while(sensorDataRS.next())
				{					
					Date d = DATE_FORMAT.parse(sensorDataRS.getString(2));
					float xg = sensorDataRS.getFloat(3);
					float yg = sensorDataRS.getFloat(4);
					float xa = sensorDataRS.getFloat(5);
					float ya = sensorDataRS.getFloat(6);
					float za = sensorDataRS.getFloat(7);
					float al = sensorDataRS.getFloat(8);
					SensorData data = new SensorData(d, xg, yg, xa, ya, za, al);
					sensor._data.add(data);
					if(sensor._data.size() >= config.getMaxData())
					{
						break;
					}
				}
				
				sensorList.add(sensor);
				logger.log(Level.INFO, "등록된 장치 로드:"+id);
				
			}
		}
		catch (SQLException | ParseException e)
		{
			logger.log(Level.SEVERE, "장치 조회 실패", e);
		}
		return sensorList;
	}
	
	void saveSensorState(Sensor s)
	{
		StringBuffer query = new StringBuffer();
		query.append("update Sensor set lastUpdateTime='");
		query.append(DATE_FORMAT.format(s.getLastUpdateTime()));
		query.append("', online=");
		query.append(s.isOnline() ? 1 : 0);
		query.append(" where id=");
		query.append(s.id);
		query.append(";");
		this.dbHandler.executeQuery(query.toString());
		
		this.dbHandler.executeQuery("delete from Sensor_Log where id="+s.id+";");
		this.dbHandler.executeQuery("delete from Sensor_Data where id="+s.id+";");
		
		for(SensorLog data : s.log)
		{
			StringBuffer buf = new StringBuffer();
			buf.append("insert into Sensor_Log values(");
			buf.append(s.id); buf.append(", '");
			buf.append(DATE_FORMAT.format(data.time)); buf.append("', '");
			buf.append(data.level.toString()); buf.append("', '");
			buf.append(data.message);
			buf.append("');");
			this.dbHandler.executeQuery(buf.toString());
		}
		
		for(SensorData data : s.data)
		{
			StringBuffer buf = new StringBuffer();
			buf.append("insert into Sensor_Data values(");
			buf.append(s.id); buf.append(", '");
			buf.append(DATE_FORMAT.format(data.time)); buf.append("', ");
			buf.append(data.X_GRADIANT); buf.append(", ");
			buf.append(data.Y_GRADIANT); buf.append(", ");
			buf.append(data.X_ACCEL); buf.append(", ");
			buf.append(data.Y_ACCEL); buf.append(", ");
			buf.append(data.Z_ACCEL); buf.append(", ");
			buf.append(data.Altitiude);
			buf.append(");");
			this.dbHandler.executeQuery(buf.toString());
		}
	}
	
	void createSensor(Sensor s)
	{
		StringBuffer query = new StringBuffer();
		query.append("insert into Sensor values(");
		query.append(s.id);
		query.append(", '");
		query.append(DATE_FORMAT.format(s.getLastUpdateTime()));
		query.append("', ");
		query.append(s.isOnline() ? 1 : 0);
		query.append(");");
		this.dbHandler.executeQuery(query.toString());
	}
	
	void destroySensor(Sensor sensor)
	{
		this.dbHandler.executeQuery("delete from Sensor where id="+sensor.id);
	}
}
