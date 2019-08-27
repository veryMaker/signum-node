package brs.services.impl;

import brs.DependencyProvider;
import brs.at.AT;
import brs.db.store.ATStore;
import brs.services.ATService;

import java.util.Collection;
import java.util.List;

public class ATServiceImpl implements ATService {

  private final DependencyProvider dp;

  public ATServiceImpl(DependencyProvider dp) {
    this.dp = dp;
  }

  @Override
  public Collection<Long> getAllATIds() {
    return dp.atStore.getAllATIds();
  }

  @Override
  public List<Long> getATsIssuedBy(Long accountId) {
    return dp.atStore.getATsIssuedBy(accountId);
  }

  @Override
  public AT getAT(Long id) {
    return dp.atStore.getAT(id);
  }

}
