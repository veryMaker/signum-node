package brs.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ServerConnectorFactory {

  private final WebServerContext context;
  private final Server server;

  public ServerConnectorFactory(WebServerContext context, Server server) {
    this.context = context;
    this.server = server;
  }

  public ServerConnector createHttpConnector() {
    return new ServerConnectorHttpBuilderImpl(context).build(server);
  }
  public ServerConnector createWebsocketConnector(ServletContextHandler servletContextHandler) {
    return new ServerConnectorWebsocketBuilderImpl(context, servletContextHandler).build(server);
  }

}
