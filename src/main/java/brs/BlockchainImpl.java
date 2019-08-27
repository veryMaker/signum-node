package brs;

import brs.db.BlockDb;
import brs.db.TransactionDb;
import brs.db.store.BlockchainStore;
import brs.util.StampedLockUtils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public class BlockchainImpl implements Blockchain {

  private final DependencyProvider dp;
  
  private final StampedLock bcsl;
  
  BlockchainImpl(DependencyProvider dp) {
    this.dp = dp;
    this.bcsl = new StampedLock();
  }

  private final AtomicReference<Block> lastBlock = new AtomicReference<>();

  private <T> T bcslRead(Supplier<T> supplier) {
    return StampedLockUtils.stampedLockRead(bcsl, supplier);
  }

  @Override
  public Block getLastBlock() {
    return bcslRead(lastBlock::get);
  }

  @Override
  public void setLastBlock(Block block) {
    long stamp = bcsl.writeLock();
    try {
      lastBlock.set(block);
    } finally {
      bcsl.unlockWrite(stamp);
    }
  }

  @Override
  public void setLastBlock(Block previousBlock, Block block) {
    long stamp = bcsl.writeLock();
    try {
      if (! lastBlock.compareAndSet(previousBlock, block)) {
        throw new IllegalStateException("Last block is no longer previous block");
      }
    } finally {
      bcsl.unlockWrite(stamp);
    }
  }

  @Override
  public int getHeight() {  
    Block last = getLastBlock();
    return last == null ? 0 : last.getHeight();
  }
    
  @Override
  public Block getLastBlock(int timestamp) {
    Block block = getLastBlock();
    if (timestamp >= block.getTimestamp()) {
      return block;
    }
    return dp.dbs.getBlockDb().findLastBlock(timestamp);
  }

  @Override
  public Block getBlock(long blockId) {
    Block block = getLastBlock();
    if (block.getId() == blockId) {
      return block;
    }
    return dp.dbs.getBlockDb().findBlock(blockId);
  }

  @Override
  public boolean hasBlock(long blockId) {
    return getLastBlock().getId() == blockId || dp.dbs.getBlockDb().hasBlock(blockId);
  }

  @Override
  public Collection<Block> getBlocks(int from, int to) {
    return dp.blockchainStore.getBlocks(from, to);
  }

  @Override
  public Collection<Block> getBlocks(Account account, int timestamp) {
    return getBlocks(account, timestamp, 0, -1);
  }

  @Override
  public Collection<Block> getBlocks(Account account, int timestamp, int from, int to) {
    return dp.blockchainStore.getBlocks(account, timestamp, from, to);
  }

  @Override
  public Collection<Long> getBlockIdsAfter(long blockId, int limit) {
    return dp.blockchainStore.getBlockIdsAfter(blockId, limit);
  }

  @Override
  public Collection<Block> getBlocksAfter(long blockId, int limit) {
    return dp.blockchainStore.getBlocksAfter(blockId, limit);
  }

  @Override
  public long getBlockIdAtHeight(int height) {
    Block block = getLastBlock();
    if (height > block.getHeight()) {
      throw new IllegalArgumentException("Invalid height " + height + ", current blockchain is at " + block.getHeight());
    }
    if (height == block.getHeight()) {
      return block.getId();
    }
    return dp.dbs.getBlockDb().findBlockIdAtHeight(height);
  }

  @Override
  public Block getBlockAtHeight(int height) {
    Block block = getLastBlock();
    if (height > block.getHeight()) {
      throw new IllegalArgumentException("Invalid height " + height + ", current blockchain is at " + block.getHeight());
    }
    if (height == block.getHeight()) {
      return block;
    }
    return dp.dbs.getBlockDb().findBlockAtHeight(height);
  }

  @Override
  public Transaction getTransaction(long transactionId) {
    return dp.dbs.getTransactionDb().findTransaction(transactionId);
  }

  @Override
  public Transaction getTransactionByFullHash(String fullHash) {
    return dp.dbs.getTransactionDb().findTransactionByFullHash(fullHash);
  }

  @Override
  public boolean hasTransaction(long transactionId) {
    return dp.dbs.getTransactionDb().hasTransaction(transactionId);
  }

  @Override
  public boolean hasTransactionByFullHash(String fullHash) {
    return dp.dbs.getTransactionDb().hasTransactionByFullHash(fullHash);
  }

  @Override
  public int getTransactionCount() {
    return dp.blockchainStore.getTransactionCount();
  }

  @Override
  public Collection<Transaction> getAllTransactions() {
    return dp.blockchainStore.getAllTransactions();
  }

  @Override
  public Collection<Transaction> getTransactions(Account account, byte type, byte subtype, int blockTimestamp, boolean includeIndirectIncoming) {
    return getTransactions(account, 0, type, subtype, blockTimestamp, 0, -1, includeIndirectIncoming);
  }

  @Override
  public Collection<Transaction> getTransactions(Account account, int numberOfConfirmations, byte type, byte subtype,
                                                 int blockTimestamp, int from, int to, boolean includeIndirectIncoming) {
    return dp.blockchainStore.getTransactions(account, numberOfConfirmations, type, subtype, blockTimestamp, from, to, includeIndirectIncoming);
  }
}
