package brs.web.api.http.handler;

import brs.Account;
import brs.Attachment;
import brs.Blockchain;
import brs.BurstException;
import brs.Constants;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.NOT_ENOUGH_FUNDS;
import static brs.web.api.http.common.JSONResponses.ERROR_NOT_ALLOWED;
import static brs.web.api.http.common.Parameters.AMOUNT_NQT_PARAMETER;

public final class RemoveCommitment extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public RemoveCommitment(ParameterService parameterService, Blockchain blockchain, AccountService accountService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS, LegacyDocTag.MINING, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, AMOUNT_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final Account account = parameterService.getSenderAccount(req);
    long amountNQT = ParameterParser.getAmountNQT(req);

    int nBlocksMined = blockchain.getBlocksCount(account.getId(), blockchain.getHeight() - Constants.MAX_ROLLBACK, blockchain.getHeight());
    if(nBlocksMined > 0) {
      // need to wait since the last block mined to remove any commitment
      return ERROR_NOT_ALLOWED;
    }

    long committedAmountNQT = blockchain.getCommittedAmount(account.getId(), blockchain.getHeight(), blockchain.getHeight(), null);
    if (committedAmountNQT < amountNQT) {
      return NOT_ENOUGH_FUNDS;
    }
    Attachment attachment = new Attachment.CommitmentRemove(amountNQT, blockchain.getHeight());
    return createTransaction(req, account, attachment);
  }

}
