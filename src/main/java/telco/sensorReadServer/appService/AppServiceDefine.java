package telco.sensorReadServer.appService;

import java.text.SimpleDateFormat;

public class AppServiceDefine
{
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYYMMddHHmmss");
	
	public static final String REQ_SensorData = "reqSensorData";
	public static final byte SensorData_PROTO_REQ_DEVICEID = 0;
	public static final byte SensorData_PROTO_REP_ALLDATA = 1;
	public static final byte SensorData_PROTO_REP_REALTIMEDATA = 2;
}
