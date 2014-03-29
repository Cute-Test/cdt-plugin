package ch.hsr.ifs.mockator.plugin.base.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class HigherOrder {

  public static <T> Collection<T> filter(Iterable<T> elements, F1<T, Boolean> predicate) {
    List<T> filtered = new ArrayList<T>();

    for (T e : elements) {
      if (predicate.apply(e)) {
        filtered.add(e);
      }
    }

    return filtered;
  }

  public static <T> Collection<T> filter(T[] elements, F1<T, Boolean> predicate) {
    return filter(Arrays.asList(elements), predicate);
  }

  public static <S, T> Collection<T> map(Iterable<S> elements, F1<S, T> f) {
    List<T> mapped = new ArrayList<T>();

    for (S e : elements) {
      mapped.add(f.apply(e));
    }

    return mapped;
  }

  public static <T, Y> Y fold(Iterable<T> elements, Injector<T, Y> injector) {
    for (T e : elements) {
      injector.apply(e);
    }

    return injector.yield();
  }

  public static <T> void forEach(Iterable<F1V<T>> funs, T param) {
    for (F1V<T> f : funs) {
      f.apply(param);
    }
  }

  public static <T> void forEach(Iterable<F1V<T>> funs, T param, F1<Void, Boolean> stopWhen) {
    for (F1V<T> f : funs) {
      if (!stopWhen.apply(null)) {
        f.apply(param);
      }
    }
  }
}
