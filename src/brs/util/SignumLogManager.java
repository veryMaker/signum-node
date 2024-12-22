package brs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

/**
 * Java LogManager extension for use with Signum
 */
class SignumLogManager extends LogManager {

  /**
   * Logging reconfiguration in progress
   */
  private final AtomicBoolean loggingReconfiguration = new AtomicBoolean(false);

  /**
   * Create the Signum log manager
   *
   * We will let the Java LogManager create its shutdown hook so that the
   * shutdown context will be set up properly.  However, we will intercept
   * the reset() method so we can delay the actual shutdown until we are
   * done terminating the Signum processes.
   */
  public SignumLogManager() {
    super();
  }

  /**
   * Reconfigure logging support using a configuration file
   *
   * @param       inStream            Input stream
   * @throws      IOException         Error reading input stream
   * @throws      SecurityException   Caller does not have LoggingPermission("control")
   */
  @Override
  public void readConfiguration(InputStream inStream) throws IOException {
    loggingReconfiguration.set(true);
    super.readConfiguration(inStream);
    loggingReconfiguration.set(false);
  }

  /**
   * Reset the log handlers
   *
   * This method is called to reset the log handlers.  We will forward the
   * call during logging reconfiguration but will ignore it otherwise.
   * This allows us to continue to use logging facilities during Signum shutdown.
   */
  @Override
  public void reset() {
    if (loggingReconfiguration.get())
      super.reset();
  }

  /**
   * Signum shutdown is now complete, so call LogManager.reset() to terminate
   * the log handlers.
   */
  void signumShutdown() {
    super.reset();
  }
}
