package brs.web.api.http.handler;

import brs.Asset;
import brs.Blockchain;
import brs.Burst;
import brs.BurstException;
import brs.assetexchange.AssetExchange;
import brs.common.AbstractUnitTest;
import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.fluxcapacitor.FluxCapacitor;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.JSON;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.ASSET_PARAMETER;
import static brs.web.api.http.common.ResultFields.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Burst.class)
public class GetAssetTest extends AbstractUnitTest {

  private ParameterService parameterServiceMock;
  private AssetExchange mockAssetExchange;
  private AccountService mockAccountService;

  private GetAsset t;

  @Before
  public void setUp() {
    parameterServiceMock = mock(ParameterService.class);
    mockAssetExchange = mock(AssetExchange.class);
    mockAccountService = mock(AccountService.class);
    FluxCapacitor mockFluxCapacitor = QuickMocker.latestValueFluxCapacitor();

    mockStatic(Burst.class);
    Blockchain mockBlockchain = mock(Blockchain.class);
    when(Burst.getBlockchain()).thenReturn(mockBlockchain);
    when(mockBlockchain.getHeight()).thenReturn(Integer.MAX_VALUE);
    when(Burst.getFluxCapacitor()).thenReturn(mockFluxCapacitor);

    t = new GetAsset(parameterServiceMock, mockAssetExchange, mockAccountService);
  }

  @Test
  public void processRequest() throws BurstException {
    final long assetId = 4;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(ASSET_PARAMETER, assetId)
    );

    final Asset asset = mock(Asset.class);
    when(asset.getId()).thenReturn(assetId);
    when(asset.getName()).thenReturn("assetName");
    when(asset.getDescription()).thenReturn("assetDescription");
    when(asset.getDecimals()).thenReturn(Byte.parseByte("3"));

    when(parameterServiceMock.getAsset(eq(req))).thenReturn(asset);

    int tradeCount = 1;
    int transferCount = 2;
    int assetAccountsCount = 3;

    when(mockAssetExchange.getTradeCount(eq(assetId))).thenReturn(tradeCount);
    when(mockAssetExchange.getTransferCount(eq(assetId))).thenReturn(transferCount);
    when(mockAssetExchange.getAssetAccountsCount(eq(asset), eq(0L), eq(true), eq(false))).thenReturn(assetAccountsCount);

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertNotNull(result);
    assertEquals(asset.getName(), JSON.getAsString(result.get(NAME_RESPONSE)));
    assertEquals(asset.getDescription(), JSON.getAsString(result.get(DESCRIPTION_RESPONSE)));
    assertEquals(asset.getDecimals(), JSON.getAsInt(result.get(DECIMALS_RESPONSE)));
    assertEquals("" + asset.getQuantityQNT(), JSON.getAsString(result.get(QUANTITY_QNT_RESPONSE)));
    assertEquals("" + asset.getId(), JSON.getAsString(result.get(ASSET_RESPONSE)));
    assertEquals(tradeCount, JSON.getAsInt(result.get(NUMBER_OF_TRADES_RESPONSE)));
    assertEquals(transferCount, JSON.getAsInt(result.get(NUMBER_OF_TRANSFERS_RESPONSE)));
    assertEquals(assetAccountsCount, JSON.getAsInt(result.get(NUMBER_OF_ACCOUNTS_RESPONSE)));
  }
}
