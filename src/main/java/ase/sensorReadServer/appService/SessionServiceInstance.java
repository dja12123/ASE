package ase.sensorReadServer.appService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.clientSession.ChannelEvent;
import ase.clientSession.IChannel;
import ase.clientSession.ISession;
import ase.console.LogWriter;
import ase.sensorReadServer.appService.serviceInstance.RealtimeSensorDataSender;
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
	
	private Map<IChannel, ServiceInstance> serviceInstMap;
	
	public SessionServiceInstance(ISession session, SensorManager sensorManager)
	{
		this.session = session;
		this.sensorManager = sensorManager;
		this.channelObserver = this::channelObserver;
		this.serviceInstMap = new HashMap<>();
		this.session.addChannelObserver(this.channelObserver);
	}
	
	public void destroy()
	{
		this.session.removeChannelObserver(this.channelObserver);
		HashSet<ServiceInstance> set = new HashSet<>();
		set.addAll(this.serviceInstMap.values());
		for(ServiceInstance inst : this.serviceInstMap.values())
		{
			inst.destroy();
		}
		this.serviceInstMap.clear();
	}

	private void channelObserver(Observable<ChannelEvent> provider, ChannelEvent event)
	{
		if(event.isOpen)
		{
			ServiceInstance serviceInst = null;
			switch(event.channel.getKey())
			{
			case SensorListSender.KEY:
				serviceInst = new SensorListSender(event.channel, this.sensorManager);
				break;
			case RealtimeSensorDataSender.KEY:
				serviceInst = new RealtimeSensorDataSender(event.channel, this.sensorManager);
				break;
			}
			if(serviceInst != null)
			{
				logger.log(Level.INFO, "서비스 요청 key:"+serviceInst.key);
				this.serviceInstMap.put(event.channel, serviceInst);
				serviceInst.startService();
			}
		}
		else
		{
			ServiceInstance serviceInst = this.serviceInstMap.getOrDefault(event.channel, null);
			if(serviceInst != null)
			{
				serviceInst.destroy();
				this.serviceInstMap.remove(event.channel);
			}
		}

	}
}
