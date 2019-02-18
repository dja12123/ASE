package telco.sensorReadServer.appService;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.Connection;
import telco.appConnect.channel.AppDataPacketBuilder;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ChannelEvent;
import telco.appConnect.channel.ProtocolDefine;
import telco.console.LogWriter;
import telco.sensorReadServer.sensorManager.SensorManager;
import telco.sensorReadServer.sensorManager.sensor.Sensor;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class ServiceInst implements Observer<ChannelEvent>
{
	public static final Logger logger = LogWriter.createLogger(ServiceInst.class, "serviceInst");
	
	private Connection connection;
	private SensorManager sensorManager;
	private SensorDataSender sensorDataSender;
	
	ServiceInst(Connection connection, SensorManager sensorManager)
	{
		this.connection = connection;
		this.sensorManager = sensorManager;
		this.connection.addObserver(this);
	}
	
	void destroy()
	{
		this.connection.removeObserver(this);
		this.chCloseSensorData();
	}

	@Override
	public void update(Observable<ChannelEvent> object, ChannelEvent data)
	{
		if(data.channel.isOpen())
		{
			switch(data.channel.key)
			{
			case AppServiceDefine.CHKEY_SensorData:
				this.chCreateSensorData(data.channel);
				break;
			case AppServiceDefine.CHKEY_SensorList:
				this.chCreateSensorList(data.channel);
				break;
			}
		}
		else
		{
			switch(data.channel.key)
			{
			case AppServiceDefine.CHKEY_SensorData:
				this.chCloseSensorData();
				break;
			}
		}
		
	}
	
	private void chCreateSensorData(Channel ch)
	{
		if(this.sensorDataSender != null)
		{
			this.sensorDataSender.destroy();
		}
		logger.log(Level.INFO, "센서 데이터 전송 요구");
		this.sensorDataSender = new SensorDataSender(ch, this.sensorManager);
	}
	
	private void chCloseSensorData()
	{
		if(this.sensorDataSender != null)
		{
			logger.log(Level.INFO, "센서 데이터 전송 요구 종료");
			this.sensorDataSender.destroy();
			this.sensorDataSender = null;
		}
	}
	
	private void chCreateSensorList(Channel ch)
	{
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(ProtocolDefine.intToByteArray(this.sensorManager.sensorMap.size()));
		for(Sensor sensor : this.sensorManager.sensorMap.values())
		{
			ByteBuffer buf = ByteBuffer.allocate(4 + 1);
			buf.putInt(sensor.id);
			buf.put((byte)(sensor.isOnline() ? 1 : 0));
			b.appendData(buf.array());
		}
		
		ch.sendData(b);
	}
}
