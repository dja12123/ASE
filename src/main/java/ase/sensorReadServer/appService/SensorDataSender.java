package ase.sensorReadServer.appService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ase.clientSession.ChannelDataEvent;
import ase.clientSession.IChannel;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.DataReceiveEvent;
import ase.sensorReadServer.sensorManager.sensor.Sensor;
import ase.sensorReadServer.sensorManager.sensor.SensorData;
import ase.sensorReadServer.sensorManager.sensor.SensorLog;
import ase.sensorReadServer.sensorManager.sensor.SensorOnlineEvent;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SensorDataSender
{
	private IChannel channel;
	private SensorManager sensorManager;
	private Sensor sensor;
	
	private Observer<DataReceiveEvent> dataReceiveObserver;
	private Observer<SensorLog> sensorLogObserver;
	private Observer<SensorOnlineEvent> sensorOnlineObserver;
	private Observer<ChannelDataEvent> channelDataObserver;
	
	SensorDataSender(IChannel channel, SensorManager sensorManager)
	{
		this.channel = channel;
		this.sensorManager = sensorManager;
		
		this.dataReceiveObserver = this::dataReceiveCallback;
		this.sensorLogObserver = this::logReceiveCallback;
		this.sensorOnlineObserver = this::onlineEventCallback;
		this.channelDataObserver = this::channelDataObserver;
		
		this.channel.addDataReceiveObserver(this.channelDataObserver);
	}
	
	public void destroy()
	{
		this.channel.removeDataReceiveObserver(this.channelDataObserver);
		if(this.sensor != null)
		{
			this.sensor.dataReceiveObservable.removeObserver(this.dataReceiveObserver);
			this.sensor.sensorLogObservable.removeObserver(this.sensorLogObserver);
			this.sensor.sensorOnlineObservable.removeObserver(this.sensorOnlineObserver);
		}
	}

	public void channelDataObserver(Observable<ChannelDataEvent> provider, ChannelDataEvent event)
	{
		switch(event.data[0])
		{
		case AppServiceDefine.SensorData_REQ_DEVICEID:
			this.reqDeviceIDTask(event.channel, event.data);
			break;
		case AppServiceDefine.SensorData_REQ_ALLDATA:
			this.sendAllSensorDataTask(event.data);
			break;
		case AppServiceDefine.SensorData_REQ_ALLLOG:
			this.sendAllLogDataTask(event.data);
			break;
		}
	}

	public void dataReceiveCallback(Observable<DataReceiveEvent> object, DataReceiveEvent e)
	{
		ByteBuffer buf = ByteBuffer.allocate(1+AppServiceDefine.DATE_FORMAT_SIZE+8+4+4+4+4+4+4);
		buf.put(AppServiceDefine.SensorData_REP_REALTIMEDATA);
		buf.put(AppServiceDefine.DATE_FORMAT.format(e.data.time).getBytes());
		buf.putFloat(e.data.X_GRADIANT);
		buf.putFloat(e.data.Y_GRADIANT);
		buf.putFloat(e.data.X_ACCEL);
		buf.putFloat(e.data.Y_ACCEL);
		buf.putFloat(e.data.Z_ACCEL);
		buf.putFloat(e.data.Altitiude);
		this.channel.sendData(buf.array());
	}
	
	public void logReceiveCallback(Observable<SensorLog> object, SensorLog l)
	{
		byte[] msg = l.message.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(1+4+AppServiceDefine.DATE_FORMAT_SIZE+msg.length);
		buf.put(AppServiceDefine.SensorData_REP_REALTIMELOG);
		buf.putInt(l.level.intValue());
		buf.put(AppServiceDefine.DATE_FORMAT.format(l.time).getBytes());
		buf.put(msg);
		buf.put(buf.array());
		this.channel.sendData(buf.array());
	}
	
	public void onlineEventCallback(Observable<SensorOnlineEvent> object, SensorOnlineEvent e)
	{
		ByteBuffer buf = ByteBuffer.allocate(1+1);
		buf.put(AppServiceDefine.SensorData_REP_REALTIMEONOFF);
		buf.put((byte)(e.isOnline ? 1 : 0));
		this.channel.sendData(buf.array());
	}
	
	private void reqDeviceIDTask(IChannel ch, byte[] data)
	{
		ByteBuffer getIdBuf = ByteBuffer.wrap(data);
		getIdBuf.position(1);
		int id = getIdBuf.getInt();
		
		this.sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
		boolean isValid = this.sensor != null ? true : false;
		if(isValid)
		{
			this.sensor.dataReceiveObservable.addObserver(this.dataReceiveObserver);
			this.sensor.sensorLogObservable.addObserver(this.sensorLogObserver);
			this.sensor.sensorOnlineObservable.addObserver(this.sensorOnlineObserver);
		}
		ByteBuffer buf = ByteBuffer.allocate(1+1+1);
		buf.put(AppServiceDefine.SensorData_REP_DEVICEID);
		buf.put((byte)(isValid ? 1 : 0));
		buf.put((byte)(isValid && this.sensor.isOnline() ? 1 : 0));
		this.channel.sendData(buf.array());

	}
	
	private void sendAllLogDataTask(byte[] data)
	{
		ByteBuffer getSizeBuf = ByteBuffer.wrap(data);
		getSizeBuf.position(1);
		int size = getSizeBuf.getInt();
		
		List<SensorLog> logs = new ArrayList<>(this.sensor.log.size());
		logs.addAll(this.sensor.log);
		
		int sendStart = logs.size() - size;
		if(sendStart < 0) sendStart = 0;
		
	
		byte[][] byteLogs = new byte[logs.size()-sendStart][];
		int allSize = 0;
		
		for(int i = sendStart; i < logs.size(); ++i)
		{
			byte[] blog = logs.get(i).message.getBytes();
			byteLogs[i - sendStart] = blog;
			allSize += (short)blog.length;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(1+4+(byteLogs.length*2)+allSize);
		ByteBuffer dataInput = ByteBuffer.wrap(buf.array());
		buf.put(AppServiceDefine.SensorData_REP_ALLLOG);
		buf.putInt(byteLogs.length);
		dataInput.position(1+4+(byteLogs.length*2));
		for(byte[] logData : byteLogs)
		{
			buf.putShort((short)logData.length);
			dataInput.put(logData);
		}
		this.channel.sendData(buf.array());
	}
	
	private void sendAllSensorDataTask(byte[] data)
	{
		ByteBuffer getSizeBuf = ByteBuffer.wrap(data);
		getSizeBuf.position(1);
		int size = getSizeBuf.getInt();
		
		List<SensorData> sensorDatas = new ArrayList<>(this.sensor.data.size());
		sensorDatas.addAll(this.sensor.data);
		
		int sendStart = sensorDatas.size() - size;
		if(sendStart < 0) sendStart = 0;
		
		ByteBuffer buf = ByteBuffer.allocate(1+4+((sensorDatas.size()-sendStart)*(AppServiceDefine.DATE_FORMAT_SIZE+4+4+4+4+4+4)));
		
		buf.put(AppServiceDefine.SensorData_REP_ALLDATA);
		buf.putInt(sensorDatas.size() - sendStart);
		
		for(int i = sendStart; i < sensorDatas.size(); ++i)
		{
			SensorData d = sensorDatas.get(i);
			buf.put(AppServiceDefine.DATE_FORMAT.format(d.time).getBytes());
			buf.putFloat(d.X_GRADIANT);
			buf.putFloat(d.Y_GRADIANT);
			buf.putFloat(d.X_ACCEL);
			buf.putFloat(d.Y_ACCEL);
			buf.putFloat(d.Z_ACCEL);
			buf.putFloat(d.Altitiude);
		}
		this.channel.sendData(buf.array());
	}
}
