package ase.util.observer;

import ase.util.observer.Observable;

public interface Observer<ObservedType>
{
    public void update(Observable<ObservedType> object, ObservedType data);
}