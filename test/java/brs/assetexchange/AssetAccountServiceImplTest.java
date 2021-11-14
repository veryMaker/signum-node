package brs.assetexchange;

import brs.Account.AccountAsset;
import brs.Asset;
import brs.db.store.AccountStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetAccountServiceImplTest {

  private AssetAccountServiceImpl t;

  private AccountStore mockAccountStore;

  @Before
  public void setUp() {
    mockAccountStore = mock(AccountStore.class);

    t = new AssetAccountServiceImpl(mockAccountStore);
  }

  @Test
  public void getAssetAccounts() {
    final int from = 1;
    final int to = 5;
    final Asset mockAsset = mock(Asset.class);
    final ArrayList<AccountAsset> mockAccountIterator = new ArrayList<>();
    
    when(mockAccountStore.getAssetAccounts(eq(mockAsset), eq(false), eq(0L), eq(from), eq(to))).thenReturn(mockAccountIterator);

    assertEquals(mockAccountIterator, t.getAssetAccounts(mockAsset, false, 0L, from, to));
  }

  @Test
  public void getAssetAccountsCount() {
    when(mockAccountStore.getAssetAccountsCount(eq(123L), eq(0L))).thenReturn(5);

    assertEquals(5L, t.getAssetAccountsCount(123, eq(0L)));
  }
}
