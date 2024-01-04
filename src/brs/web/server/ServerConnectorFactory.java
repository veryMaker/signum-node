package brs.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class ServerConnectorFactory {

  private static ServerConnectorFactory instance;
  private final WebServerContext context;
  private final Server server;

  public static ServerConnectorFactory getInstance(WebServerContext context, Server server) {
    if(instance == null) {
      instance = new ServerConnectorFactory(context, server);
    }
    return instance;
  }
  private ServerConnectorFactory(WebServerContext context, Server server) {
    this.context = context;
    this.server = server;
  }

  public ServerConnector createHttpConnector() {
    return new ServerConnectorHttpBuilderImpl(context).build(server);
  }
  public ServerConnector createWebsocketConnector() {
    return new ServerConnectorWebsocketBuilderImpl(context).build(server);
  }

}
