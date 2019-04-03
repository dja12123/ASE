package ase.clientSession;

import ase.util.observer.Observer;

public interface IChannel
{
	public String getKey();
	public boolean isOpen();
	public void addDataReceiveObserver(Observer<ChannelDataEvent> observer);
	public void removeDataReceiveObserver(Observer<ChannelDataEvent> observer);
	public void addChannelCloseObserver(Observer<IChannel> observer);
	public void removeChannelCloseObserver(Observer<IChannel> observer);
	public void sendData(byte[] data);
	public void close();
}
