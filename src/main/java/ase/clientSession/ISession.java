package ase.clientSession;

import ase.util.observer.Observer;

public interface ISession
{
	public void addChannelObserver(Observer<ChannelEvent> observer);
	public void removeChannelObserver(Observer<ChannelEvent> observer);
	public void close();
}
