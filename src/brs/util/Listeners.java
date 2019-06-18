package brs.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Listeners<T,E extends Enum<E>> {
  private final ConcurrentHashMap<Enum<E>, List<Consumer<T>>> listenersMap = new ConcurrentHashMap<>();

  public boolean addListener(Consumer<T> listener, Enum<E> eventType) {
    synchronized (this) {
        List<Consumer<T>> listeners = listenersMap.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        return listeners.add(listener);
    }
  }

  public boolean removeListener(Consumer<T> listener, Enum<E> eventType) {
    synchronized (this) {
      List<Consumer<T>> listeners = listenersMap.get(eventType);
      if (listeners != null) {
        return listeners.remove(listener);
      }
    }
    return false;
  }

  public void accept(T t, Enum<E> eventType) {
    List<Consumer<T>> listeners = listenersMap.get(eventType);
    if (listeners != null) {
      for (Consumer<T> listener : listeners) {
        listener.accept(t);
      }
    }
  }
}
