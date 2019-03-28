package ase.web.session;

public class SessionEvent
{
	public final Session session;
	public final boolean isActive;
	
	public SessionEvent(Session session, boolean isActive)
	{
		this.session = session;
		this.isActive = isActive;
	}
}
