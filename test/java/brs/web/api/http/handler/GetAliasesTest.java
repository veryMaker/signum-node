package brs.web.api.http.handler;

import brs.Account;
import brs.Alias;
import brs.Alias.Offer;
import brs.SignumException;
import brs.common.AbstractUnitTest;
import brs.common.QuickMocker;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.ResultFields.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetAliasesTest extends AbstractUnitTest {

  private GetAliases t;

  private ParameterService mockParameterService;
  private AliasService mockAliasService;

  @Before
  public void setUp() {
    mockParameterService = mock(ParameterService.class);
    mockAliasService = mock(AliasService.class);

    t = new GetAliases(mockParameterService, mockAliasService);
  }

  @Test
  public void processRequest() throws SignumException {
    final long accountId = 123L;
    final HttpServletRequest req = QuickMocker.httpServletRequest();

    final Account mockAccount = mock(Account.class);
    when(mockAccount.getId()).thenReturn(accountId);

    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(567L);

    final Offer mockOffer = mock(Offer.class);
    when(mockOffer.getPriceNqt()).thenReturn(234L);

    final CollectionWithIndex<Alias> mockAliasIterator = new CollectionWithIndex<Alias>(mockCollection(mockAlias), 0, 1);

    when(mockParameterService.getAccount(eq(req), eq(false))).thenReturn(mockAccount);

    when(mockAliasService.getAliasesByOwner(eq(accountId), isNull(), isNull(), eq(0), eq(499))).thenReturn(mockAliasIterator);
    when(mockAliasService.getOffer(eq(mockAlias))).thenReturn(mockOffer);

    final JsonObject resultOverview = (JsonObject) t.processRequest(req);
    assertNotNull(resultOverview);

    final JsonArray resultList = (JsonArray) resultOverview.get(ALIASES_RESPONSE);
    assertNotNull(resultList);
    assertEquals(1, resultList.size());

    final JsonObject result = (JsonObject) resultList.get(0);
    assertNotNull(result);
    assertEquals("" +mockAlias.getId(), JSON.getAsString(result.get(ALIAS_RESPONSE)));
    assertEquals("" + mockOffer.getPriceNqt(), JSON.getAsString(result.get(PRICE_NQT_RESPONSE)));
  }

}
