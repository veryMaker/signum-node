package brs.web.api.ws.common;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleScheduler {

  private final ScheduledThreadPoolExecutor executor;
  private final int intervalSecs;
  private final int shutdownTimeoutSecs;

  public SimpleScheduler(int intervalSecs, int shutdownTimeoutSecs){
    this.intervalSecs = intervalSecs;
    this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    executor = new java.util.concurrent.ScheduledThreadPoolExecutor(1);
  }

  public void start(Runnable task){
    executor.scheduleAtFixedRate(task, 0, intervalSecs, TimeUnit.SECONDS);
  }

  public void shutdown(){
    if(executor.isTerminated()){
      return;
    }

    executor.shutdown();
    try {
      if (!executor.awaitTermination(shutdownTimeoutSecs, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
