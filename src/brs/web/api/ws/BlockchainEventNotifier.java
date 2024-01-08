package brs.web.api.ws;

import brs.*;
import brs.web.api.ws.handler.BlockGeneratedEventHandler;
import brs.web.api.ws.handler.PendingTransactionsAddedEventHandler;
import brs.web.api.ws.handler.TestEventHandler;
import brs.web.server.WebServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class BlockchainEventNotifier {

    private final static int SHUTDOWN_TIMEOUT_SECS = 5;
    private final static int IO_THREAD_COUNT = 10;
    private final ExecutorService executor;
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
        this.context.getBlockchainProcessor().addListener(this::onNewBlockEvent, BlockchainProcessor.Event.BLOCK_GENERATED);
        this.context.getTransactionProcessor().addListener(this::onPendingTransactionEvent, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(wrap(this::onTestEvent), 0, 10_000);
    }

    private static TimerTask wrap(Runnable r) {
        return new TimerTask() {

            @Override
            public void run() {
                r.run();
            }
        };
    }

    public void addConnection(WebSocketConnection connection) {
        connections.put(connection.getId(), connection);
    }

    public void removeConnection(WebSocketConnection connection) {
        connections.remove(connection.getId());
    }

    public void onTestEvent() {
        this.executor.submit(() -> {
            connections.values().forEach(connection -> new TestEventHandler(connection).notify("some test data"));
        });
    }

    public void onNewBlockEvent(Block block) {
        this.executor.submit(() -> {
            connections.values().forEach(connection -> new BlockGeneratedEventHandler(connection).notify(block));
        });
    }

    private void onPendingTransactionEvent(List<? extends Transaction> transactions) {
      this.executor.submit(() -> {
        connections.values().forEach(connection -> new PendingTransactionsAddedEventHandler(connection).notify(transactions));
      });
    }

    public void shutdown() {
        if (!executor.isTerminated()) {
            logger.info("Closing {} websocket connection(s)...", connections.size());
            executor.submit(() -> {
                connections.values().forEach(WebSocketConnection::close);
            });
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
