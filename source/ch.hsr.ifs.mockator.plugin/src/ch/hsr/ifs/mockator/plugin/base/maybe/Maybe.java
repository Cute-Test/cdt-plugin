package ch.hsr.ifs.mockator.plugin.base.maybe;

import java.util.Iterator;
import java.util.NoSuchElementException;

// Inspired by
// https://www.iam.unibe.ch/scg/svn_repos/Sources/ch.akuhn.util/src/ch/akuhn/util/Maybe.java
public abstract class Maybe<T> implements Iterable<T> {

  public abstract T get();

  public abstract boolean isNone();

  public abstract boolean isSome();

  private static final class None<T> extends Maybe<T> {
    private None() {}

    @Override
    public T get() {
      throw new NoSuchElementException("None has no value");
    }

    @Override
    public boolean isNone() {
      return true;
    }

    @Override
    public boolean isSome() {
      return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
      return NONE_ITER;
    }
  }

  private static final class Some<T> extends Maybe<T> {
    private final T value;

    private Some(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Some<?> && (((Some<?>) o).value).equals(value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public boolean isNone() {
      return false;
    }

    @Override
    public boolean isSome() {
      return true;
    }

    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        private boolean done = false;

        @Override
        public boolean hasNext() {
          return !done;
        }

        @Override
        public T next() {
          if (done)
            throw new NoSuchElementException();

          done = true;
          return value;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private static final None<Object> NONE = new None<Object>();

  @SuppressWarnings({"rawtypes"})
  private static final Iterator NONE_ITER = new Iterator() {
    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public Object next() {
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  };

  public static <T> Maybe<T> maybe(Maybe<T> maybe) {
    return maybe;
  }

  public static <T> Maybe<T> maybe(T t) {
    return t == null ? Maybe.<T>none() : Maybe.<T>some(t);
  }

  @SuppressWarnings("unchecked")
  public static <T> Maybe<T> none() {
    return (None<T>) NONE;
  }

  public static <T> Maybe<T> some(T t) {
    return new Some<T>(t);
  }
}
