package brs;

import brs.util.Observable;
import brs.util.ThreadPool;

import java.math.BigInteger;
import java.util.Collection;

public interface Generator extends Observable<Generator.GeneratorState, Generator.Event> {

  enum Event {
    GENERATION_DEADLINE, NONCE_SUBMITTED
  }

  void generateForBlockchainProcessor(ThreadPool threadPool, BlockchainProcessor blockchainProcessor);

  GeneratorState addNonce(String secretPhrase, Long nonce);

  GeneratorState addNonce(String secretPhrase, Long nonce, byte[] publicKey);

  Collection<GeneratorState> getAllGenerators();

  byte[] calculateGenerationSignature(byte[] lastGenSig, long lastGenId);

  int calculateScoop(byte[] genSig, long height);

  BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, int blockHeight);

  BigInteger calculateHit(long accountId, long nonce, byte[] genSig, byte[] scoopData);

  BigInteger calculateDeadline(long accountId, long nonce, byte[] genSig, int scoop, long baseTarget, int blockHeight);

  interface GeneratorState {
    byte[] getPublicKey();

    Long getAccountId();

    BigInteger getDeadline();

    long getBlock();
  }
}
