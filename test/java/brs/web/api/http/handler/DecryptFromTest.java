package brs.web.api.http.handler;

import brs.Account;
import brs.SignumException;
import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.crypto.EncryptedData;
import brs.services.ParameterService;
import brs.util.JSON;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static brs.common.TestConstants.TEST_PUBLIC_KEY_BYTES;
import static brs.common.TestConstants.TEST_SECRET_PHRASE;
import static brs.web.api.http.common.JSONResponses.INCORRECT_ACCOUNT;
import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DecryptFromTest {

  private DecryptFrom t;

  private ParameterService mockParameterService;

  @Before
  public void setUp() {
    mockParameterService = mock(ParameterService.class);

    t = new DecryptFrom(mockParameterService);
  }

  @Test
  public void processRequest() throws SignumException {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
        new MockParam(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER, "true"),
        new MockParam(DATA_PARAMETER, "abc"),
        new MockParam(NONCE_PARAMETER, "def")
    );

    final Account mockAccount = mock(Account.class);

    when(mockAccount.decryptFrom(any(EncryptedData.class), eq(TEST_SECRET_PHRASE)))
        .thenReturn(new byte[]{(byte) 1});

    when(mockAccount.getPublicKey()).thenReturn(TEST_PUBLIC_KEY_BYTES);

    when(mockParameterService.getAccount(req)).thenReturn(mockAccount);

    assertEquals("\u0001", JSON.getAsString(JSON.getAsJsonObject(t.processRequest(req)).get(DECRYPTED_MESSAGE_RESPONSE)));
  }

  @Test
  public void processRequest_accountWithoutPublicKeyIsIncorrectAccount() throws SignumException {
    final HttpServletRequest req = QuickMocker.httpServletRequest();

    when(mockParameterService.getAccount(req)).thenReturn(mock(Account.class));

    assertEquals(INCORRECT_ACCOUNT, t.processRequest(req));
  }

}
