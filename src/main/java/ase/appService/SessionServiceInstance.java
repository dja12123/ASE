package ase.appService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.appService.serviceInstance.AllSensorDataSender;
import ase.appService.serviceInstance.AllSensorLogSender;
import ase.appService.serviceInstance.RealtimeLogDataSender;
import ase.appService.serviceInstance.RealtimeSensorDataSender;
import ase.appService.serviceInstance.SensorListSender;
import ase.appService.serviceInstance.ServiceInstance;
import ase.clientSession.ChannelEvent;
import ase.clientSession.IChannel;
import ase.clientSession.ISession;
import ase.console.LogWriter;
import ase.sensorManager.SensorManager;
import ase.util.observer.Observer;

public class SessionServiceInstance
{
	public static final Logger logger = LogWriter.createLogger(SessionServiceInstance.class, "SessionServiceInstance");
	
	private ISession session;
	private SensorManager sensorManager;
	private Observer<ChannelEvent> channelObserver;
	private Map<IChannel, ServiceInstance> serviceInstMap;
	private Consumer<ServiceInstance> onDestroyInstCallback;
	
	public SessionServiceInstance(ISession session, SensorManager sensorManager)
	{
		this.session = session;
		this.sensorManager = sensorManager;
		this.channelObserver = this::channelObserver;
		this.serviceInstMap = new HashMap<>();
		this.onDestroyInstCallback = this::onDestroyInstCallback;
		this.session.addChannelObserver(this.channelObserver);
	}
	
	public synchronized void destroy()
	{
		this.session.removeChannelObserver(this.channelObserver);
		HashSet<ServiceInstance> set = new HashSet<>();
		set.addAll(this.serviceInstMap.values());
		for(ServiceInstance inst : set)
		{
			inst.destroy();
		}
		this.serviceInstMap.clear();
	}

	private synchronized void channelObserver(ChannelEvent event)
	{
		if(event.isOpen)
		{
			ServiceInstance serviceInst = null;
			switch(event.channel.getKey())
			{
			case AllSensorDataSender.KEY:
				serviceInst = new AllSensorDataSender(event.channel, this.sensorManager);
				break;
			case AllSensorLogSender.KEY:
				serviceInst = new AllSensorLogSender(event.channel, this.sensorManager);
				break;
			case RealtimeLogDataSender.KEY:
				serviceInst = new RealtimeLogDataSender(event.channel, this.sensorManager);
				break;
			case RealtimeSensorDataSender.KEY:
				serviceInst = new RealtimeSensorDataSender(event.channel, this.sensorManager);
				break;
			case SensorListSender.KEY:
				serviceInst = new SensorListSender(event.channel, this.sensorManager);
				break;
			}
			if(serviceInst != null)
			{
				logger.log(Level.INFO, "서비스 요청 key:"+serviceInst.key);
				this.serviceInstMap.put(event.channel, serviceInst);
				serviceInst.startService(this.onDestroyInstCallback);
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
	
	private synchronized void onDestroyInstCallback(ServiceInstance inst)
	{
		this.serviceInstMap.remove(inst.channel);
	}
}
