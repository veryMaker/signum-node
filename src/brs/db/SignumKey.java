package brs.db;

import org.jooq.Record;

public interface SignumKey {

  interface Factory<T> {
    SignumKey newKey(T t);

    SignumKey newKey(Record rs);
  }

  long[] getPKValues();

  interface LongKeyFactory<T> extends Factory<T> {
    @Override
    SignumKey newKey(Record rs);

    SignumKey newKey(long id);

  }

  interface LinkKeyFactory<T> extends Factory<T> {
    SignumKey newKey(long idA, long idB);
  }
  
  interface LinkKey3Factory<T> extends Factory<T> {
    SignumKey newKey(long idA, long idB, long idC);
  }
}
