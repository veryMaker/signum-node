package brs.web.api.ws.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Debouncer {

  private static final Logger logger = LoggerFactory.getLogger(Debouncer.class);

  private final int delay;
  private final Timer timer;
  private final AtomicBoolean isRunning = new AtomicBoolean(false);

  public Debouncer(int delayMillies) {
    timer = new Timer();
    isRunning.set(false);
    this.delay = delayMillies;
  }

  public void debounce(Runnable task) {
    if (isRunning.get()) {
      return;
    }
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        task.run();
        isRunning.set(false);
      }
    }, delay);

    isRunning.set(true);
  }

  public void shutdown() {
    try{
      if(isRunning.get()) {
        timer.cancel();
      }
    } catch(Exception e){
      logger.warn("Debouncer graceful shutdown failed: {}", e.getMessage());
    } finally {
      isRunning.set(false);
    }
  }
}
