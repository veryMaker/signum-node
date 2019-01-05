package brs.http;

import brs.Account;
import brs.Attachment;
import brs.BurstException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public interface APITransactionManager {

  JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, Long recipientId, long amountNQT, Attachment attachment, long minimumFeeNQT) throws BurstException;

}
