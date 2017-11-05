package ch.hsr.ifs.mockator.plugin.base.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class HigherOrder {

  public static <T> Collection<T> filter(final Iterable<T> elements, final Function<T, Boolean> predicate) {
    final List<T> filtered = new ArrayList<>();

    for (final T e : elements) {
      if (predicate.apply(e)) {
        filtered.add(e);
      }
    }

    return filtered;
  }

  public static <T> Collection<T> filter(final T[] elements, final Function<T, Boolean> predicate) {
    return filter(Arrays.asList(elements), predicate);
  }

  public static <S, T> Collection<T> map(final Iterable<S> elements, final Function<S, T> f) {
    final List<T> mapped = new ArrayList<>();

    for (final S e : elements) {
      mapped.add(f.apply(e));
    }

    return mapped;
  }

  public static <T, Y> Y fold(final Iterable<T> elements, final Injector<T, Y> injector) {
    for (final T e : elements) {
      injector.apply(e);
    }

    return injector.yield();
  }

  public static <T> void forEach(final Iterable<F1V<T>> funs, final T param) {
    for (final F1V<T> f : funs) {
      f.apply(param);
    }
  }

  public static <T> void forEach(final Iterable<F1V<T>> funs, final T param, final Function<Void, Boolean> stopWhen) {
    for (final F1V<T> f : funs) {
      if (!stopWhen.apply(null)) {
        f.apply(param);
      }
    }
  }
}
