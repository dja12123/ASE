package ase.util.observer;

public interface Observer<Event>
{
    public void update(Event event);
}