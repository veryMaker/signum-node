package brs.web.api.ws.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Debouncer2 {

  public static final int DEFAULT_TIMEOUT = 5000;
  private static final Logger logger = LoggerFactory.getLogger(Debouncer2.class);

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();


  /**
   * Debounces {@code callable} by {@code delay}, i.e., schedules it to be executed after {@code delay},
   * or cancels its execution if the method is called with the same key within the {@code delay} again.
   */
  public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
    final Future<?> prev = delayedMap.put(key, executor.schedule(() -> {
      try {
        runnable.run();
      } finally {
        delayedMap.remove(key);
      }
    }, delay, unit));
    if (prev != null) {
      prev.cancel(true);
    }
  }

  public void shutdown() {
    shutdown(DEFAULT_TIMEOUT);
  }

  public void shutdown(int timeoutSecs) {
    if(executor.isTerminated()){
      return;
    }

    executor.shutdown();
    try {
      if (!executor.awaitTermination(timeoutSecs, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
