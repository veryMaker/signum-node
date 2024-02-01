package brs.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public interface ServerConnectorBuilder {

  ServerConnector build(Server server);

}
