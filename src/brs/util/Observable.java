package brs.util;

import java.util.function.Consumer;

public interface Observable<T,E extends Enum<E>> {

  boolean addListener(Consumer<T> listener, E eventType);

  boolean removeListener(Consumer<T> listener, E eventType);

}
