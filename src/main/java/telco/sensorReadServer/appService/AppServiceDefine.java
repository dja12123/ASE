package telco.sensorReadServer.appService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppServiceDefine
{
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final int DATE_FORMAT_SIZE = DATE_FORMAT.toPattern().getBytes().length;
	
	public static final String REQ_SensorData = "reqSensorData";
	public static final byte SensorData_PROTO_REQ_DEVICEID = 0;
	public static final byte SensorData_PROTO_REP_ALLDATA = 1;
	public static final byte SensorData_PROTO_REP_REALTIMEDATA = 2;
	
	public static void main(String[] args)
	{
		Date d = new Date();
		String df = DATE_FORMAT.format(d);
		System.out.println(df.getBytes().length);
		System.out.println(DATE_FORMAT_SIZE);
	}
}
