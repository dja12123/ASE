package ase.sensorComm;

import java.util.Map;

import ase.sensorComm.protocolSerial.SerialTransmitter;
import ase.util.observer.KeyObserver;

public interface ISensorCommManager
{
	public boolean addUser(int id);
	public void removeUser(int id);
	public Map<Integer, SerialTransmitter> getUserMap();
	public void addObserver(Short key, KeyObserver<Short, ReceiveEvent> observer);
	public void removeObserver(Short key, KeyObserver<Short, ReceiveEvent> observer);
}
