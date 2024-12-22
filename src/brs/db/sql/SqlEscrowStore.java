package brs.db.sql;

import brs.Signum;
import brs.Escrow;
import brs.Transaction;
import brs.db.SignumKey;
import brs.db.VersionedEntityTable;
import brs.db.store.DerivedTableManager;
import brs.db.store.EscrowStore;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static brs.schema.Tables.ESCROW;
import static brs.schema.Tables.ESCROW_DECISION;

public class SqlEscrowStore implements EscrowStore {
  private final SignumKey.LongKeyFactory<Escrow> escrowDbKeyFactory = new DbKey.LongKeyFactory<Escrow>(ESCROW.ID) {
      @Override
      public SignumKey newKey(Escrow escrow) {
        return escrow.dbKey;
      }
    };

  private final VersionedEntityTable<Escrow> escrowTable;
  private final DbKey.LinkKeyFactory<Escrow.Decision> decisionDbKeyFactory =
      new DbKey.LinkKeyFactory<Escrow.Decision>("escrow_id", "account_id") {
        @Override
        public SignumKey newKey(Escrow.Decision decision) {
          return decision.dbKey;
        }
      };
  private final VersionedEntityTable<Escrow.Decision> decisionTable;
  private final List<Transaction> resultTransactions = new ArrayList<>();


  public SqlEscrowStore(DerivedTableManager derivedTableManager) {
    escrowTable = new VersionedEntitySqlTable<Escrow>("escrow", brs.schema.Tables.ESCROW, escrowDbKeyFactory, derivedTableManager) {
      @Override
      protected Escrow load(DSLContext ctx, Record rs) {
        return new SqlEscrow(rs);
      }

      @Override
      protected void save(DSLContext ctx, Escrow escrow) {
        saveEscrow(ctx, escrow);
      }
    };

    decisionTable = new VersionedEntitySqlTable<Escrow.Decision>("escrow_decision", brs.schema.Tables.ESCROW_DECISION, decisionDbKeyFactory, derivedTableManager) {
      @Override
      protected Escrow.Decision load(DSLContext ctx, Record record) {
        return new SqlDecision(record);
      }

      @Override
      protected void save(DSLContext ctx, Escrow.Decision decision) {
        saveDecision(ctx, decision);
      }
    };
  }

  private void saveDecision(DSLContext ctx, Escrow.Decision decision) {

    ctx.insertInto(ESCROW_DECISION,
        ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID,
        ESCROW_DECISION.DECISION, ESCROW_DECISION.HEIGHT,
        ESCROW_DECISION.LATEST)
      .values(decision.getEscrowId(), decision.getAccountId(),
        (int) Escrow.decisionToByte(decision.getDecision()),
        Signum.getBlockchain().getHeight(), true)
      .onConflict(ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.HEIGHT)
      .doUpdate()
      .set(ESCROW_DECISION.DECISION, (int) Escrow.decisionToByte(decision.getDecision()))
      .set(ESCROW_DECISION.LATEST, true)
      .execute();

  }

  @Override
  public SignumKey.LongKeyFactory<Escrow> getEscrowDbKeyFactory() {
    return escrowDbKeyFactory;
  }

  @Override
  public VersionedEntityTable<Escrow> getEscrowTable() {
    return escrowTable;
  }

  @Override
  public DbKey.LinkKeyFactory<Escrow.Decision> getDecisionDbKeyFactory() {
    return decisionDbKeyFactory;
  }

  @Override
  public VersionedEntityTable<Escrow.Decision> getDecisionTable() {
    return decisionTable;
  }

  @Override
  public Collection<Escrow> getEscrowTransactionsByParticipant(Long accountId) {
    List<Escrow> filtered = new ArrayList<>();
    for (Escrow.Decision decision : decisionTable.getManyBy(ESCROW_DECISION.ACCOUNT_ID.eq(accountId), 0, -1)) {
      Escrow escrow = escrowTable.get(escrowDbKeyFactory.newKey(decision.escrowId));
      if (escrow != null) {
        filtered.add(escrow);
      }
    }
    return filtered;
  }

  @Override
  public List<Transaction> getResultTransactions() {
    return resultTransactions;
  }

  private void saveEscrow(DSLContext ctx, Escrow escrow) {
    ctx.insertInto(ESCROW,
        ESCROW.ID, ESCROW.SENDER_ID, ESCROW.RECIPIENT_ID,
        ESCROW.AMOUNT, ESCROW.REQUIRED_SIGNERS, ESCROW.DEADLINE,
        ESCROW.DEADLINE_ACTION, ESCROW.HEIGHT, ESCROW.LATEST)
      .values(escrow.getId(), escrow.getSenderId(), escrow.getRecipientId(),
        escrow.getAmountNQT(), escrow.getRequiredSigners(),
        escrow.getDeadline(), (int) Escrow.decisionToByte(escrow.getDeadlineAction()),
        Signum.getBlockchain().getHeight(), true)
      .onConflict(ESCROW.ID, ESCROW.HEIGHT)
      .doUpdate()
      .set(ESCROW.SENDER_ID, escrow.getSenderId())
      .set(ESCROW.RECIPIENT_ID, escrow.getRecipientId())
      .set(ESCROW.AMOUNT, escrow.getAmountNQT())
      .set(ESCROW.REQUIRED_SIGNERS, escrow.getRequiredSigners())
      .set(ESCROW.DEADLINE, escrow.getDeadline())
      .set(ESCROW.DEADLINE_ACTION, (int) Escrow.decisionToByte(escrow.getDeadlineAction()))
      .set(ESCROW.LATEST, true)
      .execute();

  }

  private class SqlDecision extends Escrow.Decision {
    private SqlDecision(Record record) {
      super(decisionDbKeyFactory.newKey(record.get(ESCROW_DECISION.ESCROW_ID), record.get(ESCROW_DECISION.ACCOUNT_ID)), record.get(ESCROW_DECISION.ESCROW_ID), record.get(ESCROW_DECISION.ACCOUNT_ID),
            Escrow.byteToDecision(record.get(ESCROW_DECISION.DECISION).byteValue()));
    }
  }

  private class SqlEscrow extends Escrow {
    private SqlEscrow(Record record) {
      super(
            record.get(ESCROW.ID),
            record.get(ESCROW.SENDER_ID),
            record.get(ESCROW.RECIPIENT_ID),
            escrowDbKeyFactory.newKey(record.get(ESCROW.ID)),
            record.get(ESCROW.AMOUNT),
            record.get(ESCROW.REQUIRED_SIGNERS),
            record.get(ESCROW.DEADLINE),
            byteToDecision(record.get(ESCROW.DEADLINE_ACTION).byteValue())
            );
    }
  }

  @Override
  public Collection<Escrow.Decision> getDecisions(Long id) {
    return  decisionTable.getManyBy(ESCROW_DECISION.ESCROW_ID.eq(id), 0, -1);
  }

}
