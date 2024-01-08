package brs.web.api.ws;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebSocketConnection {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);

  private final Session session;

  public WebSocketConnection(Session session) {
    this.session = session;
  }

  public String getId() {
    return session.getRemoteAddress().toString();
  }

  public Session getSession() {
    return session;
  }

  public void sendMessage(String message) {
    RemoteEndpoint remote = this.getSession().getRemote();
    try {
      remote.sendString(message);
    } catch (IOException e) {
      logger.debug("Error sending message to {}: {}", remote.getRemoteAddress().toString(), e.getMessage());
    }
  }

  public void close() {
    this.getSession().close();
  }
}
