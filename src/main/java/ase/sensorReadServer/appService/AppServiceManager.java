package ase.sensorReadServer.appService;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.clientSession.ClientSessionManager;
import ase.clientSession.ISession;
import ase.clientSession.SessionEvent;
import ase.console.LogWriter;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.util.observer.Observable;
import ase.util.observer.Observer;

public class AppServiceManager
{
	public static final Logger logger = LogWriter.createLogger(AppServiceManager.class, "appServiceManager");
	
	private ClientSessionManager sessionManager;
	private SensorManager sensorManager;
	private HashMap<ISession, SessionServiceInstance> serviceMap;
	private Observer<SessionEvent> sessionObserver;
	
	public AppServiceManager(ClientSessionManager sessionManager, SensorManager sensorManager)
	{
		this.sessionManager = sessionManager;
		this.sensorManager = sensorManager;
		this.serviceMap = new HashMap<ISession, SessionServiceInstance>();
		this.sessionObserver = this::sessionObserver;
	}
	
	private void sessionObserver(Observable<SessionEvent> provider, SessionEvent event)
	{
		if(event.isActive)
		{
			logger.log(Level.INFO, "앱 서비스 인스턴스 생성");
			SessionServiceInstance inst = new SessionServiceInstance(event.session, this.sensorManager);
			this.serviceMap.put(event.session, inst);
		}
		else
		{
			if(this.serviceMap.containsKey(event.session))
			{
				logger.log(Level.INFO, "앱 서비스 인스턴스 삭제");
				this.serviceMap.get(event.session).destroy();
				this.serviceMap.remove(event.session);
			}
		}
	}
	
	public boolean startModule()
	{
		this.sessionManager.addObserver(this.sessionObserver);
		logger.log(Level.INFO, "앱 서비스 매니저 시작 완료");
		return true;
	}
	
	public void stopModule()
	{
		this.sessionManager.removeObserver(this.sessionObserver);
		for(SessionServiceInstance inst : this.serviceMap.values())
		{
			inst.destroy();
		}
		this.serviceMap.clear();
		logger.log(Level.INFO, "앱 서비스 매니저 종료 완료");
	}
}
