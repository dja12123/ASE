package ase.util.observer;

public interface KeyObserver<Key, Event>
{
    public void update(Key key, Event event);
}