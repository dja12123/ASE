package telco.sensorReadServer.util.observer;

import telco.sensorReadServer.util.observer.Observable;

public interface Observer<ObservedType>
{
    public void update(Observable<ObservedType> object, ObservedType data);
}