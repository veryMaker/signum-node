package brs.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConnectorWebsocketBuilderImpl implements ServerConnectorBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ServerConnectorWebsocketBuilderImpl.class);
  private final WebServerContext context;

  public ServerConnectorWebsocketBuilderImpl(WebServerContext context) {
    this.context = context;
  }
  @Override
  public ServerConnector build(Server server) {
    throw new RuntimeException("Not implemented yet");
  }
}
