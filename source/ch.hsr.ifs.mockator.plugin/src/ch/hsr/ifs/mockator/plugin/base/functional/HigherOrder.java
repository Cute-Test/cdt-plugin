package ch.hsr.ifs.mockator.plugin.base.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public abstract class HigherOrder {

   //TODO replace with stream operations
   public static <T> Collection<T> filter(final Collection<T> elements, final Predicate<T> predicate) {
      return elements.stream().filter(predicate).collect(Collectors.toList());
   }

   public static <T> Collection<T> filter(final T[] elements, final Predicate<T> predicate) {
      return Arrays.asList(elements).stream().filter(predicate).collect(Collectors.toList());
   }

   public static <T, Y> Y fold(final Iterable<T> elements, final Injector<T, Y> injector) {
      for (final T e : elements) {
         injector.accept(e);
      }

      return injector.yield();
   }
}
