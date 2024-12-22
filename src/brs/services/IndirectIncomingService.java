package brs.services;

import brs.Transaction;

public interface IndirectIncomingService {
    void processTransaction(Transaction transaction);
    boolean isIndirectlyReceiving(Transaction transaction, long accountId);
    public void rollback(int height);
}
