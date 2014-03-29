package ch.hsr.ifs.mockator.plugin.base.collections;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

// some generic type inference magic to get rid of
// boilerplate java collection creation
// some get obsolete with Java 1.7
@SuppressWarnings("unchecked")
public abstract class CollectionHelper {

  public static <T> T[] array(T... elements) {
    return elements;
  }

  public static <T> List<T> list() {
    return new ArrayList<T>();
  }

  public static <T> List<T> list(T... elements) {
    return new ArrayList<T>(asList(elements));
  }

  public static <T> List<T> list(Collection<T> elements) {
    return new ArrayList<T>(elements);
  }

  public static <T> Set<T> unorderedSet(T... elements) {
    return new HashSet<T>(asList(elements));
  }

  public static <T> Set<T> unorderedSet() {
    return new HashSet<T>();
  }

  public static <T> Set<T> orderPreservingSet() {
    return new LinkedHashSet<T>();
  }

  public static <T> Set<T> orderPreservingSet(T... elements) {
    return new LinkedHashSet<T>(asList(elements));
  }

  public static <T> Set<T> orderPreservingSet(Collection<T> elements) {
    return new LinkedHashSet<T>(elements);
  }

  public static <K, V> Map<K, V> orderPreservingMap() {
    return new LinkedHashMap<K, V>();
  }

  public static <K, V> Map<K, V> orderedMap() {
    return new TreeMap<K, V>();
  }

  public static <K, V> Map<K, V> unorderedMap() {
    return new HashMap<K, V>();
  }

  public static <K, V> Map<K, V> zipMap(K[] keys, V[] values) {
    HashMap<K, V> map = new HashMap<K, V>();

    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }
    return map;
  }

  public static <T> Collection<T> checkedCast(Collection<?> list, Class<T> klass) {
    for (Object o : list) {
      klass.cast(o);
    }
    return (Collection<T>) list;
  }

  public static <T> Map<T, T> checkedCast(Map<?, ?> map, Class<T> klass) {
    for (Entry<?, ?> e : map.entrySet()) {
      klass.cast(e.getKey());
      klass.cast(e.getValue());
    }
    return (Map<T, T>) map;
  }

  public static <T> boolean notNull(Iterable<T> it) {
    Assert.notNull(it, "iterable must not be null");

    for (T e : it)
      if (e == null)
        return false;

    return true;
  }

  public static boolean isEmpty(Iterable<?> it) {
    return !it.iterator().hasNext();
  }

  public static <E> Maybe<E> head(Iterable<E> it) {
    Iterator<E> iterator = it.iterator();
    return iterator.hasNext() ? maybe(iterator.next()) : Maybe.<E>none();
  }

  public static <E> Collection<E> tail(Iterable<E> it) {
    List<E> l = list();
    Iterator<E> i = it.iterator();
    if (i.hasNext()) {
      i.next();
      while (i.hasNext()) {
        l.add(i.next());
      }
    }
    return l;
  }

  public static <E> E head(Iterable<E> it, E defaultValue) {
    Iterator<E> iterator = it.iterator();
    return iterator.hasNext() ? iterator.next() : defaultValue;
  }

  public static <E> Maybe<E> last(E[] elements) {
    if (elements.length < 1)
      return none();
    return maybe(elements[elements.length - 1]);
  }

  public static <E> Maybe<E> last(List<E> elements) {
    if (elements.isEmpty())
      return none();
    return maybe(elements.get(elements.size() - 1));
  }

  public static <E> Iterable<E> toIterable(final Enumeration<E> e) {
    return new Iterable<E>() {
      @Override
      public Iterator<E> iterator() {
        return new Iterator<E>() {
          @Override
          public boolean hasNext() {
            return e.hasMoreElements();
          }

          @Override
          public E next() {
            return e.nextElement();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <E> boolean haveSameElementsInSameOrder(Collection<E> c1, Collection<E> c2) {
    Iterator<E> it = c2.iterator();
    for (E e : c1)
      if (!e.equals(it.next()))
        return false;
    return true;
  }
}
