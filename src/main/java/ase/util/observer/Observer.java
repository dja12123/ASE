package ase.util.observer;

import ase.util.observer.Observable;

public interface Observer<Event>
{
    public void update(Observable<Event> provider, Event event);
}