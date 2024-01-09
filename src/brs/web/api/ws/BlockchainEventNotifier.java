package brs.web.api.ws;

import brs.*;
import brs.props.Props;
import brs.web.api.ws.common.ConnectedEventData;
import brs.web.api.ws.common.Debouncer;
import brs.web.api.ws.handler.BlockGeneratedEventHandler;
import brs.web.api.ws.handler.BlockPushedEventHandler;
import brs.web.api.ws.handler.ConnectedEventHandler;
import brs.web.api.ws.handler.PendingTransactionsAddedEventHandler;
import brs.web.server.WebServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.*;

public class BlockchainEventNotifier {

  private final static int SHUTDOWN_TIMEOUT_SECS = 5;
  private final static int BLOCK_PUSHED_DEBOUNCE_SECS = 1;
  private final static int IO_THREAD_COUNT = 10;
  private final ExecutorService executor;
  private final Debouncer blockPushedDebouncer;
  private Logger logger = LoggerFactory.getLogger(BlockchainEventNotifier.class);
  private static BlockchainEventNotifier instance;
  private final WebServerContext context;

  private ConcurrentHashMap<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

  public static BlockchainEventNotifier getInstance(WebServerContext context) {
    if (BlockchainEventNotifier.instance == null) {
      BlockchainEventNotifier.instance = new BlockchainEventNotifier(context);
    }
    return BlockchainEventNotifier.instance;
  }

  private BlockchainEventNotifier(WebServerContext context) {
    this.context = context;
    this.executor = Executors.newFixedThreadPool(IO_THREAD_COUNT);
    this.context.getBlockchainProcessor().addListener(this::onBlockGeneratedEvent, BlockchainProcessor.Event.BLOCK_GENERATED);
    this.context.getBlockchainProcessor().addListener(this::onBlockPushedEvent, BlockchainProcessor.Event.BLOCK_PUSHED);
    this.context.getTransactionProcessor().addListener(this::onPendingTransactionEvent, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    this.blockPushedDebouncer = new Debouncer(BLOCK_PUSHED_DEBOUNCE_SECS * 1000);
  }

  private static TimerTask wrap(Runnable r) {
    return new TimerTask() {

      @Override
      public void run() {
        r.run();
      }
    };
  }

  private void withActiveConnectionsOnly(Runnable fn) {
    if (!this.connections.isEmpty()) {
      fn.run();
    }
  }

  public void addConnection(WebSocketConnection connection) {
    connections.put(connection.getId(), connection);

    this.executor.submit(() -> {
      ConnectedEventData data = new ConnectedEventData();
      data.version = Burst.VERSION.toString();
      data.networkName = context.getPropertyService().getString(Props.NETWORK_NAME);
      data.globalHeight = context.getBlockchainProcessor().getLastBlockchainFeederHeight();
      data.localHeight = context.getBlockchain().getHeight();
      new ConnectedEventHandler(connection).notify(data);
    });

  }

  public void removeConnection(WebSocketConnection connection) {
    connections.remove(connection.getId());
  }

  public void onBlockGeneratedEvent(Block block) {
    withActiveConnectionsOnly(() -> {
      this.executor.submit(() -> {
        connections.values().forEach(connection -> new BlockGeneratedEventHandler(connection).notify(block));
      });
    });
  }

  public void onBlockPushedEvent(Block block) {
    withActiveConnectionsOnly(() -> {
      blockPushedDebouncer.debounce(() -> {
          this.executor.submit(() -> {
            int currentHeight = context.getBlockchainProcessor().getLastBlockchainFeederHeight();
            connections.values().forEach(connection -> new BlockPushedEventHandler(connection, currentHeight).notify(block));
          });
        }
      );
    });
  }

  private void onPendingTransactionEvent(List<? extends Transaction> transactions) {
    withActiveConnectionsOnly(() -> {
      this.executor.submit(() -> {
        connections.values().forEach(connection -> new PendingTransactionsAddedEventHandler(connection).notify(transactions));
      });
    });
  }

  public void shutdown() {
    if (!executor.isTerminated()) {
      logger.info("Closing {} websocket connection(s)...", connections.size());
      executor.submit(() -> {
        connections.values().forEach(WebSocketConnection::close);
      });
      blockPushedDebouncer.shutdown();
      executor.shutdown();
      try {
        executor.awaitTermination(SHUTDOWN_TIMEOUT_SECS, TimeUnit.SECONDS); // should be able to close all connections within 5 seconds
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      if (!executor.isTerminated()) {
        logger.warn("Some threads didn't terminate, forcing shutdown");
        executor.shutdownNow();
      }
    }
  }

}
