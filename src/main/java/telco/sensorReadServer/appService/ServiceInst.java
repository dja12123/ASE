package telco.sensorReadServer.appService;

import java.util.logging.Level;
import java.util.logging.Logger;

import telco.appConnect.Connection;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ChannelEvent;
import telco.console.LogWriter;
import telco.sensorReadServer.sensorManager.SensorManager;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class ServiceInst implements Observer<ChannelEvent>
{
	public static final Logger logger = LogWriter.createLogger(ServiceInst.class, "serviceInst");
	
	private Connection connection;
	private SensorManager sensorManager;
	
	private SensorDataSender sensorDataSender;
	private SensorDeviceDataSender sensorDeviceDataSender;
	
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
			case AppServiceDefine.CHKEY_SensorDeviceData:
				this.chCreateSensorDeviceData(data.channel);
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
			case AppServiceDefine.CHKEY_SensorDeviceData:
				this.chCloseSensorList();
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
	
	private void chCreateSensorDeviceData(Channel ch)
	{
		if(this.sensorDeviceDataSender != null)
		{
			this.sensorDeviceDataSender.destroy();
		}
		logger.log(Level.INFO, "센서 장치 정보 요구");
		this.sensorDeviceDataSender = new SensorDeviceDataSender(ch, this.sensorManager);
	}
	
	private void chCloseSensorList()
	{
		if(this.sensorDeviceDataSender != null)
		{
			logger.log(Level.INFO, "센서 장치 정보 요구 종료");
			this.sensorDeviceDataSender.destroy();
			this.sensorDeviceDataSender = null;
		}
	}
}
