//
// ========================================================================
// Copyright (c) Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package brs.web.api.ws;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import brs.Block;
import brs.BlockchainProcessor;
import brs.web.server.WebServerContext;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// https://itnext.io/writing-a-web-socket-server-with-embedded-jetty-46fe9ab1c435 -- check this.
public class EventHandler extends WebSocketAdapter {
  private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
  private final CountDownLatch closureLatch = new CountDownLatch(1);
  private final WebServerContext context;

  private Map<String, Session> connections = Collections.synchronizedMap(new HashMap<>());

  public EventHandler(WebServerContext context) {
    this.context = context;
  }


  @Override
  public void onWebSocketConnect(Session sess) {
    super.onWebSocketConnect(sess);
    logger.debug("Endpoint connected: {}", sess);
    connections.put(sess.toString(), sess);

    // add listeners to blockchain and transaction processor
    // must be thread safe
//    context.getBlockchainProcessor().addListener(EventHandler::notify, BlockchainProcessor.Event.BLOCK_GENERATED)
  }

  @Override
  public void onWebSocketText(String message) {
    super.onWebSocketText(message);
    logger.debug("Received TEXT message: {}", message);
    try {
      this.getRemote().sendString("Echo: " + message);
    } catch (Exception e) {
      logger.warn("Failed to send WS message", e);
    }
    if (message.toLowerCase(Locale.US).contains("bye")) {
      getSession().close(StatusCode.NORMAL, "Thanks");
    }
  }


  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    super.onWebSocketClose(statusCode, reason);
    logger.debug("Socket Closed: [{}] {}", statusCode, reason);
    closureLatch.countDown();
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    super.onWebSocketError(cause);
    cause.printStackTrace(System.err);
  }


  public void awaitClosure() throws InterruptedException {
    logger.debug("Awaiting closure from remote");
    closureLatch.await();
  }
}
