//package ch.hsr.ifs.mockator.plugin.base.;
//
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//
//
//// Inspired by
//// https://www.iam.unibe.ch/scg/svn_repos/Sources/ch.akuhn.util/src/ch/akuhn/util/.java
//public abstract class <T> implements Iterable<T> {
//
//   public abstract T get();
//
//   public abstract boolean isNone();
//
//   public abstract boolean isSome();
//
//   private static final class None<T> extends <T> {
//
//      private None() {}
//
//      @Override
//      public T get() {
//         throw new NoSuchElementException("None has no value");
//      }
//
//      @Override
//      public boolean isNone() {
//         return true;
//      }
//
//      @Override
//      public boolean isSome() {
//         return false;
//      }
//
//      @Override
//      @SuppressWarnings("unchecked")
//      public Iterator<T> iterator() {
//         return NONE_ITER;
//      }
//   }
//
//   private static final class Some<T> extends <T> {
//
//      private final T value;
//
//      private Some(final T value) {
//         this.value = value;
//      }
//
//      @Override
//      public T get() {
//         return value;
//      }
//
//      @Override
//      public boolean equals(final Object o) {
//         return o instanceof Some<?> && (((Some<?>) o).value).equals(value);
//      }
//
//      @Override
//      public int hashCode() {
//         return value.hashCode();
//      }
//
//      @Override
//      public boolean isNone() {
//         return false;
//      }
//
//      @Override
//      public boolean isSome() {
//         return true;
//      }
//
//      @Override
//      public Iterator<T> iterator() {
//         return new Iterator<T>() {
//
//            private boolean done = false;
//
//            @Override
//            public boolean hasNext() {
//               return !done;
//            }
//
//            @Override
//            public T next() {
//               if (done) { throw new NoSuchElementException(); }
//
//               done = true;
//               return value;
//            }
//
//            @Override
//            public void remove() {
//               throw new UnsupportedOperationException();
//            }
//         };
//      }
//   }
//
//   private static final None<Object> NONE = new None<>();
//
//   @SuppressWarnings({ "rawtypes" })
//   private static final Iterator NONE_ITER = new Iterator() {
//
//      @Override
//      public boolean hasNext() {
//         return false;
//      }
//
//      @Override
//      public Object next() {
//         throw new NoSuchElementException();
//      }
//
//      @Override
//      public void remove() {
//         throw new UnsupportedOperationException();
//      }
//   };
//
//   public static <T> <T> (final <T> ) {
//      return ;
//   }
//
//   public static <T> <T> (final T t) {
//      return t == null ? .<T>none() : .<T>some(t);
//   }
//
//   @SuppressWarnings("unchecked")
//   public static <T> <T> none() {
//      return (None<T>) NONE;
//   }
//
//   public static <T> <T> some(final T t) {
//      return new Some<>(t);
//   }
//}
