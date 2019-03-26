package ase.web.session;

import ase.sensorReadServer.ServerCore;

public class SessionConfigAccess
{
	public static final String PROP_SESSION_TIMEOUT = "SessionTimeout";
	
	private int sessionTimeout;
	
	public SessionConfigAccess()
	{
		this.updateFromConfig();
	}
	
	public void updateFromConfig()
	{
		this.sessionTimeout = Integer.parseInt(ServerCore.getProp(PROP_SESSION_TIMEOUT));
	}
	
	public int getSessionTimeout()
	{
		return this.sessionTimeout;
	}
}
