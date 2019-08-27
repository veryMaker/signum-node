package brs.db.sql;

import brs.DependencyProvider;
import brs.db.BlockDb;
import brs.db.PeerDb;
import brs.db.TransactionDb;
import brs.db.store.Dbs;

public class SqlDbs implements Dbs {

  private final BlockDb blockDb;
  private final TransactionDb transactionDb;
  private final PeerDb peerDb;

  public SqlDbs(DependencyProvider dp) {
    this.blockDb       = new SqlBlockDb(dp);
    this.transactionDb = new SqlTransactionDb(dp);
    this.peerDb        = new SqlPeerDb();
  }

  @Override
  public BlockDb getBlockDb() {
    return blockDb;
  }

  @Override
  public TransactionDb getTransactionDb() {
    return transactionDb;
  }

  @Override
  public PeerDb getPeerDb() {
    return peerDb;
  }
}
