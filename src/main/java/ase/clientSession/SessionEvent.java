package ase.clientSession;

public class SessionEvent
{
	public final ISession session;
	public final boolean isActive;
	
	public SessionEvent(ISession session, boolean isActive)
	{
		this.session = session;
		this.isActive = isActive;
	}
}
