package brs.web.api.ws.common;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Debouncer {
  private final int delay;
  private Timer timer;
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
    if(isRunning.get()) {
      timer.cancel();
      isRunning.set(false);
    }
  }
}
