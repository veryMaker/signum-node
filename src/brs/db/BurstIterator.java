package brs.db;

import org.jooq.DSLContext;

import java.sql.ResultSet;
import java.util.Iterator;

public interface BurstIterator<T> extends Iterator<T>, AutoCloseable {
  @Override
  boolean hasNext();

  @Override
  T next();

  @Override
  void remove();

  @Override
  void close();

  interface ResultSetReader<T> {
    T get(DSLContext ctx, ResultSet rs) throws Exception;
  }
}
