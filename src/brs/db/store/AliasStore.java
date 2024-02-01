package brs.db.store;

import brs.Alias;
import brs.db.SignumKey;
import brs.db.VersionedEntityTable;

import java.util.Collection;

public interface AliasStore {
  SignumKey.LongKeyFactory<Alias> getAliasDbKeyFactory();
  SignumKey.LongKeyFactory<Alias.Offer> getOfferDbKeyFactory();

  VersionedEntityTable<Alias> getAliasTable();

  VersionedEntityTable<Alias.Offer> getOfferTable();

  Collection<Alias> getAliasesByOwner(long accountId, String name, Long tld, int from, int to);

  Collection<Alias> getTLDs(int from, int to);
  
  Collection<Alias.Offer> getAliasOffers(long account, long buyer, int from, int to);

  Alias getAlias(String aliasName, long tld);
  
  Alias getTLD(String tldName);
  
  Alias getTLD(long tldId);
}
