package ch.hsr.ifs.mockator.plugin.base.data;

import ch.hsr.ifs.iltis.core.data.AbstractPair;


public class Pair<T1, T2> extends AbstractPair<T1, T2> {

   public Pair(final T1 first, final T2 second) {
      super(first, second);
   }

   public T1 first() {
      return first;
   }

   public T2 second() {
      return second;
   }

   public static <ST1, ST2> Pair<ST1, ST2> from(final ST1 first, final ST2 second) {
      return new Pair<>(first, second);
   }
}
