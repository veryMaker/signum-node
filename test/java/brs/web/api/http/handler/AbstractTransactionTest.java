package brs.web.api.http.handler;

import brs.Account;
import brs.Attachment;
import brs.SignumException;
import brs.common.AbstractUnitTest;
import brs.web.api.http.common.APITransactionManager;
import com.google.gson.JsonElement;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractTransactionTest extends AbstractUnitTest {

  @FunctionalInterface
  public interface TransactionCreationFunction<R> {
    R apply() throws SignumException;
  }

  protected Attachment attachmentCreatedTransaction(TransactionCreationFunction r, APITransactionManager apiTransactionManagerMock) throws SignumException {
    final ArgumentCaptor<Attachment> ac = ArgumentCaptor.forClass(Attachment.class);

    when(apiTransactionManagerMock.createTransaction(any(HttpServletRequest.class), nullable(Account.class), nullable(Long.class), anyLong(), ac.capture(), anyLong())).thenReturn(mock(JsonElement.class));

    r.apply();

    return ac.getValue();
  }

}
