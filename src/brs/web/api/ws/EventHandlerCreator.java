package brs.web.api.ws;

import brs.web.server.WebServerContext;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;

public class EventHandlerCreator implements JettyWebSocketCreator {

  private final WebServerContext context;

  public EventHandlerCreator(WebServerContext context) {
    this.context = context;
  }

    @Override
  public Object createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest, JettyServerUpgradeResponse jettyServerUpgradeResponse) {
    return new EventHandler(this.context);
  }
}
