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
	
	ServiceInst(Connection connection, SensorManager sensorManager)
	{
		this.connection = connection;
		this.sensorManager = sensorManager;
		this.connection.addObserver(this);
	}
	
	void destroy()
	{
		this.connection.removeObserver(this);
		this.reqSensorDataDestroyTask();
	}

	@Override
	public void update(Observable<ChannelEvent> object, ChannelEvent data)
	{
		if(data.channel.isOpen())
		{
			switch(data.channel.key)
			{
			case AppServiceDefine.REQ_SensorData:
				this.reqSensorDataCreateTask(data.channel);
				break;
			}
		}
		else
		{
			switch(data.channel.key)
			{
			case AppServiceDefine.REQ_SensorData:
				this.reqSensorDataDestroyTask();
				break;
			}
		}
		
	}
	
	private void reqSensorDataCreateTask(Channel channel)
	{
		if(this.sensorDataSender != null)
		{
			this.sensorDataSender.destroy();
		}
		logger.log(Level.INFO, "센서 데이터 전송 요구");
		new Exception().printStackTrace();
		this.sensorDataSender = new SensorDataSender(channel, this.sensorManager);
	}
	
	private void reqSensorDataDestroyTask()
	{
		logger.log(Level.INFO, "센서 데이터 전송 요구 종료");
		if(this.sensorDataSender != null)
		{
			this.sensorDataSender.destroy();
			this.sensorDataSender = null;
		}
	}
}
