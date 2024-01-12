package brs.web.api.ws.common;

import java.util.concurrent.*;

public class SimpleScheduler {

  private final ScheduledExecutorService executor;
  private final int intervalSecs;
  private final int shutdownTimeoutSecs;
  private final Runnable periodicTask;
  private ScheduledFuture<?> scheduledFuture;
  public SimpleScheduler(int intervalSecs, int shutdownTimeoutSecs, Runnable task){
    this.intervalSecs = intervalSecs;
    this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    this.periodicTask = task;
    executor = Executors.newSingleThreadScheduledExecutor();
  }

  public void start(){
    stop();
    scheduledFuture = executor.scheduleAtFixedRate(periodicTask, intervalSecs, intervalSecs, TimeUnit.SECONDS);
  }

  public void stop(){
    if(scheduledFuture != null){
      scheduledFuture.cancel(false);
      scheduledFuture = null;
    }
  }
  public void pause(){
    stop();
  }
  public void resume(){
    start();
  }
  public void shutdown(){

    if(executor.isTerminated()){
      return;
    }

    try {
      if(scheduledFuture != null){
        scheduledFuture.cancel(false);
      }
      executor.shutdown();
      if (!executor.awaitTermination(shutdownTimeoutSecs, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
