package brs.services.impl;

import brs.Alias;
import brs.Signum;
import brs.Alias.Offer;
import brs.Attachment.MessagingAliasAssignment;
import brs.Attachment.MessagingAliasSell;
import brs.Transaction;
import brs.common.AbstractUnitTest;
import brs.common.QuickMocker;
import brs.db.BurstKey;
import brs.db.BurstKey.LongKeyFactory;
import brs.db.VersionedEntityTable;
import brs.db.store.AliasStore;
import brs.fluxcapacitor.FluxCapacitor;
import brs.fluxcapacitor.FluxValues;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Signum.class)
public class AliasServiceImplTest extends AbstractUnitTest {

  private AliasServiceImpl t;

  private AliasStore aliasStoreMock;
  private VersionedEntityTable<Alias> aliasTableMock;
  private BurstKey.LongKeyFactory<Alias> aliasDbKeyFactoryMock;
  private VersionedEntityTable<Offer> offerTableMock;
  private BurstKey.LongKeyFactory<Offer> offerDbKeyFactoryMock;

  @Before
  public void setUp() {
    mockStatic(Signum.class);

    aliasStoreMock = mock(AliasStore.class);
    aliasTableMock = mock(VersionedEntityTable.class);
    aliasDbKeyFactoryMock = mock(LongKeyFactory.class);
    offerTableMock = mock(VersionedEntityTable.class);
    offerDbKeyFactoryMock = mock(LongKeyFactory.class);

    FluxCapacitor mockFluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_POC2, FluxValues.DIGITAL_GOODS_STORE);
    when(Signum.getFluxCapacitor()).thenReturn(mockFluxCapacitor);

    when(aliasStoreMock.getAliasTable()).thenReturn(aliasTableMock);
    when(aliasStoreMock.getAliasDbKeyFactory()).thenReturn(aliasDbKeyFactoryMock);
    when(aliasStoreMock.getOfferTable()).thenReturn(offerTableMock);
    when(aliasStoreMock.getOfferDbKeyFactory()).thenReturn(offerDbKeyFactoryMock);

    t = new AliasServiceImpl(aliasStoreMock);
  }

  @Test
  public void getAlias() {
    final String aliasName = "aliasName";
    final Alias mockAlias = mock(Alias.class);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    assertEquals(mockAlias, t.getAlias(aliasName, 0L));
  }

  @Test
  public void getAlias_byId() {
    final long id = 123l;
    final BurstKey mockKey = mock(BurstKey.class);
    final Alias mockAlias = mock(Alias.class);

    when(aliasDbKeyFactoryMock.newKey(eq(id))).thenReturn(mockKey);
    when(aliasTableMock.get(eq(mockKey))).thenReturn(mockAlias);

    assertEquals(mockAlias, t.getAlias(id));
  }

  @Test
  public void getOffer() {
    final Long aliasId = 123l;
    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(aliasId);
    final BurstKey mockOfferKey = mock(BurstKey.class);
    final Offer mockOffer = mock(Offer.class);

    when(offerDbKeyFactoryMock.newKey(eq(aliasId))).thenReturn(mockOfferKey);
    when(offerTableMock.get(eq(mockOfferKey))).thenReturn(mockOffer);

    assertEquals(mockOffer, t.getOffer(mockAlias));
  }

  @Test
  public void getAliasCount() {
    when(aliasTableMock.getCount()).thenReturn(5);
    assertEquals(5L, t.getAliasCount());
  }

  @Test
  public void getAliasesByOwner() {
    final long accountId = 123L;
    final int from = 0;
    final int to = 1;

    final Collection<Alias> mockAliasIterator = mockCollection();

    when(aliasStoreMock.getAliasesByOwner(eq(accountId), isNull(), eq(0L), eq(from), eq(to))).thenReturn(mockAliasIterator);

    assertEquals(mockAliasIterator, t.getAliasesByOwner(accountId, null, 0L, from, to).getCollection());
  }

  @Test
  public void addOrUpdateAlias_addAlias() {
    final Transaction transaction = mock(Transaction.class);
    when(transaction.getSenderId()).thenReturn(123L);
    when(transaction.getBlockTimestamp()).thenReturn(34);

    final MessagingAliasAssignment attachment = mock(MessagingAliasAssignment.class);
    when(attachment.getAliasURI()).thenReturn("aliasURI");

    t.addOrUpdateAlias(transaction, attachment);

    final ArgumentCaptor<Alias> savedAliasCaptor = ArgumentCaptor.forClass(Alias.class);

    verify(aliasTableMock).insert(savedAliasCaptor.capture());

    final Alias savedAlias = savedAliasCaptor.getValue();
    assertNotNull(savedAlias);

    assertEquals(transaction.getSenderId(), savedAlias.getAccountId());
    assertEquals(transaction.getBlockTimestamp(), savedAlias.getTimestamp());
    assertEquals(attachment.getAliasURI(), savedAlias.getAliasURI());
  }

  @Test
  public void addOrUpdateAlias_updateAlias() {
    final String aliasName = "aliasName";
    final Alias mockAlias = mock(Alias.class);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    final Transaction transaction = mock(Transaction.class);
    when(transaction.getSenderId()).thenReturn(123L);
    when(transaction.getBlockTimestamp()).thenReturn(34);

    final MessagingAliasAssignment attachment = mock(MessagingAliasAssignment.class);
    when(attachment.getAliasName()).thenReturn(aliasName);
    when(attachment.getAliasURI()).thenReturn("aliasURI");

    t.addOrUpdateAlias(transaction, attachment);

    verify(mockAlias).setAccountId(eq(transaction.getSenderId()));
    verify(mockAlias).setTimestamp(eq(transaction.getBlockTimestamp()));
    verify(mockAlias).setAliasURI(eq(attachment.getAliasURI()));

    verify(aliasTableMock).insert(eq(mockAlias));
  }

  @Test
  public void sellAlias_forBurst_newOffer() {
    final String aliasName = "aliasName";
    final long aliasId = 123L;
    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(aliasId);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    final BurstKey mockOfferKey = mock(BurstKey.class);
    when(offerDbKeyFactoryMock.newKey(eq(aliasId))).thenReturn(mockOfferKey);

    final long priceNQT = 500L;

    final long newOwnerId = 234L;
    final int timestamp = 567;

    final Transaction transaction = mock(Transaction.class);
    final MessagingAliasSell attachment = mock(MessagingAliasSell.class);
    when(attachment.getAliasName()).thenReturn(aliasName);
    when(attachment.getPriceNQT()).thenReturn(priceNQT);
    when(transaction.getBlockTimestamp()).thenReturn(timestamp);
    when(transaction.getRecipientId()).thenReturn(newOwnerId);

    t.sellAlias(transaction, attachment);

    ArgumentCaptor<Offer> mockOfferCaptor = ArgumentCaptor.forClass(Offer.class);

    verify(offerTableMock).insert(mockOfferCaptor.capture());

    final Offer savedOffer = mockOfferCaptor.getValue();
    assertEquals(newOwnerId, savedOffer.getBuyerId());
    assertEquals(priceNQT, savedOffer.getPriceNQT());
  }

  @Test
  public void sellAlias_forBurst_offerExists() {
    final String aliasName = "aliasName";
    final long aliasId = 123L;
    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(aliasId);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    final BurstKey mockOfferKey = mock(BurstKey.class);
    final Offer mockOffer = mock(Offer.class);
    when(offerDbKeyFactoryMock.newKey(eq(aliasId))).thenReturn(mockOfferKey);
    when(offerTableMock.get(eq(mockOfferKey))).thenReturn(mockOffer);

    final long priceNQT = 500L;

    final long newOwnerId = 234L;
    final int timestamp = 567;

    final Transaction transaction = mock(Transaction.class);
    final MessagingAliasSell attachment = mock(MessagingAliasSell.class);
    when(attachment.getAliasName()).thenReturn(aliasName);
    when(attachment.getPriceNQT()).thenReturn(priceNQT);
    when(transaction.getBlockTimestamp()).thenReturn(timestamp);
    when(transaction.getRecipientId()).thenReturn(newOwnerId);

    t.sellAlias(transaction, attachment);

    verify(mockOffer).setPriceNQT(eq(priceNQT));
    verify(mockOffer).setBuyerId(eq(newOwnerId));

    verify(offerTableMock).insert(eq(mockOffer));
  }

  @Test
  public void sellAlias_forFree() {
    final String aliasName = "aliasName";
    final long aliasId = 123L;
    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(aliasId);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    final BurstKey mockOfferKey = mock(BurstKey.class);
    final Offer mockOffer = mock(Offer.class);
    when(offerDbKeyFactoryMock.newKey(eq(aliasId))).thenReturn(mockOfferKey);
    when(offerTableMock.get(eq(mockOfferKey))).thenReturn(mockOffer);

    final long priceNQT = 0L;

    final long newOwnerId = 234L;
    final int timestamp = 567;

    final Transaction transaction = mock(Transaction.class);
    final MessagingAliasSell attachment = mock(MessagingAliasSell.class);
    when(attachment.getAliasName()).thenReturn(aliasName);
    when(attachment.getPriceNQT()).thenReturn(priceNQT);
    when(transaction.getBlockTimestamp()).thenReturn(timestamp);
    when(transaction.getRecipientId()).thenReturn(newOwnerId);

    t.sellAlias(transaction, attachment);

    verify(mockAlias).setAccountId(newOwnerId);
    verify(mockAlias).setTimestamp(eq(timestamp));
    verify(aliasTableMock).insert(mockAlias);

    verify(offerTableMock).delete(eq(mockOffer));
  }

  @Test
  public void changeOwner() {
    final String aliasName = "aliasName";
    final long aliasId = 123L;
    final Alias mockAlias = mock(Alias.class);
    when(mockAlias.getId()).thenReturn(aliasId);

    when(aliasStoreMock.getAlias(eq(aliasName), eq(0L))).thenReturn(mockAlias);

    final BurstKey mockOfferKey = mock(BurstKey.class);
    final Offer mockOffer = mock(Offer.class);
    when(offerDbKeyFactoryMock.newKey(eq(aliasId))).thenReturn(mockOfferKey);
    when(offerTableMock.get(eq(mockOfferKey))).thenReturn(mockOffer);

    final long newOwnerId = 234L;
    final int timestamp = 567;

    t.changeOwner(newOwnerId, mockAlias, timestamp, true);

    verify(mockAlias).setAccountId(newOwnerId);
    verify(mockAlias).setTimestamp(eq(timestamp));
    verify(aliasTableMock).insert(mockAlias);

    verify(offerTableMock).delete(eq(mockOffer));
  }
}
