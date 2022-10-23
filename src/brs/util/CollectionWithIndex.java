package brs.util;

import java.util.Collection;
import java.util.Iterator;

public class CollectionWithIndex<E> implements Iterable<E> {

  private int nextIndex = -1;
  private Collection<E> collection;
  
  public CollectionWithIndex(Collection<E> collection, int nextIndex) {
    this.nextIndex = nextIndex;
    this.collection = collection;
  }

  public CollectionWithIndex(Collection<E> collection, int from, int to) {
    this.nextIndex = collection.size() == to-from+1 ? to+1 : -1;
    this.collection = collection;
  }

  public Collection<E> getCollection() {
    return collection;
  }
  
  public boolean hasNextIndex() {
    return nextIndex > 0;
  }
  
  public int nextIndex() {
    return nextIndex;
  }
  
  public void setNextIndex(int nextIndex) {
    this.nextIndex = nextIndex;
  }

  @Override
  public Iterator<E> iterator() {
    return collection.iterator();
  }
  
  public int size() {
    return collection.size();
  }
  
}
