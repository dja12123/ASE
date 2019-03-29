package ase.sensorReadServer.appService;

import java.util.logging.Level;
import java.util.logging.Logger;

import ase.clientSession.ChannelEvent;
import ase.clientSession.IChannel;
import ase.clientSession.ISession;
import ase.console.LogWriter;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class ServiceInst
{
	public static final Logger logger = LogWriter.createLogger(ServiceInst.class, "serviceInst");
	
	private ISession session;
	private SensorManager sensorManager;
	
	private SensorDataSender sensorDataSender;
	private AllSensorDataSender sensorDeviceDataSender;
	
	private Observer<ChannelEvent> channelObserver;
	
	public ServiceInst(ISession session, SensorManager sensorManager)
	{
		this.session = session;
		this.sensorManager = sensorManager;
		this.channelObserver = this::channelObserver;
		this.session.addChannelObserver(this.channelObserver);
	}
	
	public void destroy()
	{
		this.session.removeChannelObserver(this.channelObserver);
		this.chCloseSensorData();
		this.chCloseSensorDeviceData();
	}

	private void channelObserver(Observable<ChannelEvent> provider, ChannelEvent event)
	{
		if(event.isOpen)
		{
			switch(event.channel.getKey())
			{
			case AppServiceDefine.CHKEY_SensorData:
				this.chCreateSensorData(event.channel);
				break;
			case AppServiceDefine.CHKEY_SensorDeviceData:
				this.chCreateSensorDeviceData(event.channel);
				break;
			}
		}
		else
		{
			switch(event.channel.getKey())
			{
			case AppServiceDefine.CHKEY_SensorData:
				this.chCloseSensorData();
				break;
			case AppServiceDefine.CHKEY_SensorDeviceData:
				this.chCloseSensorDeviceData();
				break;
			}
		}
		
	}
	
	private void chCreateSensorData(IChannel ch)
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
	
	private void chCreateSensorDeviceData(IChannel ch)
	{
		if(this.sensorDeviceDataSender != null)
		{
			this.sensorDeviceDataSender.destroy();
		}
		logger.log(Level.INFO, "센서 장치 정보 요구");
		this.sensorDeviceDataSender = new AllSensorDataSender(ch, this.sensorManager);
	}
	
	private void chCloseSensorDeviceData()
	{
		if(this.sensorDeviceDataSender != null)
		{
			logger.log(Level.INFO, "센서 장치 정보 요구 종료");
			this.sensorDeviceDataSender.destroy();
			this.sensorDeviceDataSender = null;
		}
	}
}
