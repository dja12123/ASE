package ase.sensorReadServer.appService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.clientSession.ChannelEvent;
import ase.clientSession.ISession;
import ase.console.LogWriter;
import ase.sensorReadServer.appService.serviceInstance.SensorListSender;
import ase.sensorReadServer.appService.serviceInstance.ServiceInstance;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class SessionServiceInstance
{
	public static final Logger logger = LogWriter.createLogger(SessionServiceInstance.class, "SessionServiceInstance");
	
	private ISession session;
	private SensorManager sensorManager;
	
	private Observer<ChannelEvent> channelObserver;
	private Consumer<ServiceInstance> serviceInstCloseCallback;
	
	private List<ServiceInstance> serviceInstList;
	
	public SessionServiceInstance(ISession session, SensorManager sensorManager)
	{
		this.session = session;
		this.sensorManager = sensorManager;
		this.channelObserver = this::channelObserver;
		this.serviceInstCloseCallback = this::closeServiceInstanceCallback;
		this.serviceInstList = new ArrayList<>();
		this.session.addChannelObserver(this.channelObserver);
	}
	
	public void destroy()
	{
		this.session.removeChannelObserver(this.channelObserver);
	}

	private void channelObserver(Observable<ChannelEvent> provider, ChannelEvent event)
	{
		if(!event.isOpen) return;
		ServiceInstance serviceInst = null;
		switch(event.channel.getKey())
		{
		case SensorListSender.KEY:
			serviceInst = new SensorListSender(this.serviceInstCloseCallback, event.channel, this.sensorManager);
			break;
		}
		if(serviceInst != null)
		{
			logger.log(Level.INFO, "서비스 요청 key:"+serviceInst.key);
			this.serviceInstList.add(serviceInst);
			serviceInst.startService();
		}
	}
	
	private void closeServiceInstanceCallback(ServiceInstance serviceInstance)
	{
		logger.log(Level.INFO, "서비스 중지 key:"+serviceInstance.key);
		this.serviceInstList.remove(serviceInstance);
	}
}
