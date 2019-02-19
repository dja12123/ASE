package telco.sensorReadServer.appService;

import java.text.SimpleDateFormat;

public class AppServiceDefine
{
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final int DATE_FORMAT_SIZE = DATE_FORMAT.toPattern().getBytes().length;
	
	public static final String CHKEY_SensorData = "reqSensorData";
	public static final byte SensorData_PROTO_REQ_DEVICEID = 0x00;
	public static final byte SensorData_PROTO_REP_ALLDATA = 0x01;
	public static final byte SensorData_PROTO_REP_REALTIMEDATA = 0x02;
	
	public static final String CHKEY_SensorDeviceData = "reqSensorDeviceData";
	public static final byte SensorDeviceData_REQ_LIST = 0x00;
	public static final byte SensorDeviceData_REP_LIST = 0x01;
	public static final byte SensorDeviceData_REP_REALTIMEDATA = 0x10;
	public static final byte SensorDeviceData_REP_REALTIMEDATA_ONOFF = 0x11;
	public static final byte SensorDeviceData_REP_REALTIMEDATA_ADDREMOVE = 0x12;
}
