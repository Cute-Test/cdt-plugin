package ch.hsr.ifs.mockator.plugin.base.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public abstract class HigherOrder {

   //TODO replace with stream operations
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
         injector.accept(e);
      }

      return injector.yield();
   }

   public static <T> void forEach(final Iterable<Consumer<T>> funs, final T param) {
      for (final Consumer<T> f : funs) {
         f.accept(param);
      }
   }

   public static <T> void forEach(final Iterable<Consumer<T>> funs, final T param, final Function<Void, Boolean> stopWhen) {
      for (final Consumer<T> f : funs) {
         if (!stopWhen.apply(null)) {
            f.accept(param);
         }
      }
   }
}
