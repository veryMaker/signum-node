package brs.web.api.http.handler;

import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.util.JSON;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static brs.common.TestConstants.*;
import static brs.web.api.http.common.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;
import static brs.web.api.http.common.Parameters.PUBLIC_KEY_PARAMETER;
import static brs.web.api.http.common.Parameters.SECRET_PHRASE_PARAMETER;
import static brs.web.api.http.common.ResultFields.ACCOUNT_RESPONSE;
import static brs.web.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetAccountIdTest {

  private GetAccountId t;

  @Before
  public void setUp() {
    t = new GetAccountId();
  }

  @Test
  public void processRequest() {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
        new MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
    );

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertEquals(TEST_ACCOUNT_NUMERIC_ID, JSON.getAsString(result.get(ACCOUNT_RESPONSE)));
    assertEquals(TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)));
  }

  @Test
  public void processRequest_missingSecretPhraseUsesPublicKey() {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
    );

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertEquals(TEST_ACCOUNT_NUMERIC_ID, JSON.getAsString(result.get(ACCOUNT_RESPONSE)));
    assertEquals(TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)));
  }

  @Test
  public void processRequest_missingSecretPhraseAndPublicKey() {
    assertEquals(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY, t.processRequest(QuickMocker.httpServletRequest()));
  }

  @Test
  public void requirePost() {
    assertTrue(t.requirePost());
  }
}
