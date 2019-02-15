package telco.sensorReadServer.appService;

import telco.appConnect.Connection;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ChannelEvent;
import telco.sensorReadServer.sensorManager.SensorManager;
import telco.util.observer.Observable;
import telco.util.observer.Observer;

public class ServiceInst implements Observer<ChannelEvent>
{	
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
			case "reqSensorData":
				this.reqSensorDataCreateTask(data.channel);
				break;
			}
		}
		else
		{
			switch(data.channel.key)
			{
			case "reqSensorData":
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
		this.sensorDataSender = new SensorDataSender(channel, this.sensorManager);
	}
	
	private void reqSensorDataDestroyTask()
	{
		if(this.sensorDataSender != null)
		{
			this.sensorDataSender.destroy();
			this.sensorDataSender = null;
		}
	}
}
