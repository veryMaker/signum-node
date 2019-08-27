package brs.feesuggestions;

import brs.Block;
import brs.BlockchainProcessor;
import brs.BlockchainProcessor.Event;
import brs.Constants;
import brs.DependencyProvider;
import brs.db.store.BlockchainStore;

import java.util.Arrays;

import static brs.Constants.FEE_QUANT;

public class FeeSuggestionCalculator { // TODO interface

  // index 0 = oldest, length-1 = newest
  private final Block[] latestBlocks;
  private final DependencyProvider dp;
  private FeeSuggestion feeSuggestion = new FeeSuggestion(FEE_QUANT, FEE_QUANT, FEE_QUANT);

  public FeeSuggestionCalculator(DependencyProvider dp) {
    this.dp = dp;
    latestBlocks = new Block[Constants.FEE_SUGGESTION_MAX_HISTORY_LENGTH];
    dp.blockchainProcessor.addListener(this::newBlockApplied, Event.AFTER_BLOCK_APPLY);
  }

  public FeeSuggestion giveFeeSuggestion() {
    if (latestBlocksIsEmpty()) {
      fillInitialHistory();
      recalculateSuggestion();
    }

    return feeSuggestion;
  }

  private void newBlockApplied(Block block) {
    if (latestBlocksIsEmpty()) {
      fillInitialHistory();
    }

    pushNewBlock(block);
    recalculateSuggestion();
  }

  private void fillInitialHistory() {
    dp.blockchainStore.getLatestBlocks(latestBlocks.length).forEach(this::pushNewBlock);
  }

  private boolean latestBlocksIsEmpty() {
    for (Block latestBlock : latestBlocks) {
      if (latestBlock == null) return true;
    }
    return false;
  }

  private void pushNewBlock(Block block) {
    if (block == null) return;
    // Skip index 0 as we want to remove this one
    if (latestBlocks.length - 1 >= 0) System.arraycopy(latestBlocks, 1, latestBlocks, 0, latestBlocks.length - 1);
    latestBlocks[latestBlocks.length - 1] = block;
  }

  private void recalculateSuggestion() {
    try {
      int lowestAmountTransactionsNearHistory = Arrays.stream(latestBlocks).mapToInt(b -> b.getTransactions().size()).min().orElse(1);
      int averageAmountTransactionsNearHistory = (int) Math.ceil(Arrays.stream(latestBlocks).mapToInt(b -> b.getTransactions().size()).average().orElse(1));
      int highestAmountTransactionsNearHistory = Arrays.stream(latestBlocks).mapToInt(b -> b.getTransactions().size()).max().orElse(1);

      long cheapFee = (1 + lowestAmountTransactionsNearHistory) * FEE_QUANT;
      long standardFee = (1 + averageAmountTransactionsNearHistory) * FEE_QUANT;
      long priorityFee = (1 + highestAmountTransactionsNearHistory) * FEE_QUANT;

      feeSuggestion = new FeeSuggestion(cheapFee, standardFee, priorityFee);
    } catch (NullPointerException ignored) { // Can happen if there are less than latestBlocks.length blocks in the blockchain
    }
  }
}
