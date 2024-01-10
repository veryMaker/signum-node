package brs.web.server;

import brs.props.PropertyService;
import brs.props.Props;
import brs.web.api.ws.WebSocketConnectionCreator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerConnectorWebsocketBuilderImpl implements ServerConnectorBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ServerConnectorWebsocketBuilderImpl.class);
  private final WebServerContext context;
  private final ServletContextHandler servletContextHandler;

  public ServerConnectorWebsocketBuilderImpl(WebServerContext context, ServletContextHandler servletContextHandler) {
    this.context = context;
    this.servletContextHandler = servletContextHandler;
  }
  @Override
  public ServerConnector build(Server server) {
    PropertyService propertyService = context.getPropertyService();
    // TODO: WSS Support
    ServerConnector connector =  new ServerConnector(server);
    connector.setHost(propertyService.getString(Props.API_LISTEN));
    connector.setPort(propertyService.getInt(Props.API_WEBSOCKET_PORT));
    connector.setReuseAddress(true);
    JettyWebSocketServletContainerInitializer.configure(servletContextHandler, (servletContext, wsContainer) ->
    {
      wsContainer.setMaxTextMessageSize(4  * 1024);
      wsContainer.setIdleTimeout(Duration.ofSeconds(propertyService.getInt(Props.BLOCK_TIME)));
      wsContainer.addMapping("/events", new WebSocketConnectionCreator(context));
    });
    return connector;
  }

}
