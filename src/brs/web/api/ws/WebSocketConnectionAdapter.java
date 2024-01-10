
package brs.web.api.ws;

import brs.web.server.WebServerContext;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// https://itnext.io/writing-a-web-socket-server-with-embedded-jetty-46fe9ab1c435 -- check this.
public class WebSocketConnectionAdapter extends WebSocketAdapter {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionAdapter.class);
  private WebSocketConnection connection;
  private final WebServerContext context;
  private final BlockchainEventNotifier notifier;

  public WebSocketConnectionAdapter(WebServerContext context) {
    this.context = context;
    this.notifier = BlockchainEventNotifier.getInstance(context);
  }

  @Override
  public void onWebSocketConnect(Session sess) {
    super.onWebSocketConnect(sess);
    logger.debug("Endpoint connected: {}", sess);
    this.connection = new WebSocketConnection(sess);
    this.notifier.addConnection(connection);
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    super.onWebSocketClose(statusCode, reason);
    logger.debug("Socket Closed: [{}] {}", statusCode, reason);
    this.notifier.removeConnection(this.connection);
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    super.onWebSocketError(cause);
    logger.error("Socket Error: {}", cause.getMessage());
    this.notifier.removeConnection(this.connection);
  }

}
