package telco.sensorReadServer.appService;

import java.nio.ByteBuffer;

import telco.appConnect.channel.AppDataPacketBuilder;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ChannelReceiveCallback;
import telco.appConnect.channel.ProtocolDefine;
import telco.sensorReadServer.sensorManager.SensorManager;
import telco.sensorReadServer.sensorManager.sensor.DataReceiveEvent;
import telco.sensorReadServer.sensorManager.sensor.Sensor;
import telco.sensorReadServer.sensorManager.sensor.SensorData;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class SensorDataSender implements ChannelReceiveCallback, Observer<DataReceiveEvent>
{
	private Channel channel;
	private SensorManager sensorManager;
	private Sensor sensor;
	
	SensorDataSender(Channel channel, SensorManager sensorManager)
	{
		this.channel = channel;
		this.sensorManager = sensorManager;
		channel.setReceiveCallback(this);
	}
	
	public void destroy()
	{
		if(this.channel.isOpen()) this.channel.setReceiveCallback(null);
		if(this.sensor != null) this.sensor.dataReceiveObservable.removeObserver(this);
	}

	@Override
	public void receiveData(Channel ch, byte[][] data)
	{
		if(data[0][0] == AppServiceDefine.SensorData_PROTO_REQ_DEVICEID)
		{
			ByteBuffer buf = ByteBuffer.wrap(data[1]);
			int id = buf.getInt();
			this.sensor = this.sensorManager.sensorMap.getOrDefault(id, null);
			if(this.sensor == null)
			{
				return;
			}
			Thread t = new Thread(this::sendAllSensorDataTask);
			t.setDaemon(true);
			t.start();
		}
	}

	@Override
	public void update(Observable<DataReceiveEvent> object, DataReceiveEvent data)
	{
		if(this.sensor == null) return;
		this.sendRealtimeSensorDataTask(data);
	}
	
	private void sendAllSensorDataTask()
	{
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_PROTO_REP_ALLDATA);
		b.appendData(ProtocolDefine.intToByteArray(this.sensor.data.size()));
		for(SensorData d : this.sensor.data)
		{
			ByteBuffer buf = ByteBuffer.allocate(8+4+4+4+4+4+4);
			buf.putLong(d.time.getTime());
			buf.putFloat(d.X_GRADIANT);
			buf.putFloat(d.Y_GRADIANT);
			buf.putFloat(d.X_ACCEL);
			buf.putFloat(d.Y_ACCEL);
			buf.putFloat(d.Z_ACCEL);
			buf.putFloat(d.Altitiude);
			b.appendData(buf.array());
		}
		this.channel.sendData(b);
	}
	
	private void sendRealtimeSensorDataTask(DataReceiveEvent d)
	{
		Thread t = new Thread(()->{
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData(AppServiceDefine.SensorData_PROTO_REP_REALTIMEDATA);
			ByteBuffer buf = ByteBuffer.allocate(8+4+4+4+4+4+4);
			buf.putLong(d.data.time.getTime());
			buf.putFloat(d.data.X_GRADIANT);
			buf.putFloat(d.data.Y_GRADIANT);
			buf.putFloat(d.data.X_ACCEL);
			buf.putFloat(d.data.Y_ACCEL);
			buf.putFloat(d.data.Z_ACCEL);
			buf.putFloat(d.data.Altitiude);
			b.appendData(buf.array());
			this.channel.sendData(b);
		});
		
		t.setDaemon(true);
		t.start();
	}
}
