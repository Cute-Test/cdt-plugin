package ch.hsr.ifs.mockator.tests.base.maybe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public class MaybeTest {

  @Test
  public void simpleGetWithValue() {
    Maybe<Integer> maybe = Maybe.maybe(42);
    assertFalse(maybe.isNone());
    assertTrue(maybe.isSome());
    assertEquals(Integer.valueOf(42), maybe.get());
  }

  @Test(expected = NoSuchElementException.class)
  public void simpleGetWithNone() {
    Maybe<Integer> none = Maybe.none();
    assertTrue(none.isNone());
    assertFalse(none.isSome());
    none.get();
  }

  @Test
  public void sameConsideredEqual() {
    Maybe<Integer> i1 = Maybe.maybe(42);
    Maybe<Integer> i2 = Maybe.maybe(42);
    Maybe<Integer> i3 = Maybe.maybe(7);
    assertEquals(i1, i1);
    assertEquals(i2, i2);
    assertEquals(i1, i2);
    assertEquals(i2, i1);
    assertFalse(i1.equals(i3));
    assertFalse(i1.equals(Maybe.none()));
  }

  @Test
  public void noneConsideredEqual() {
    Maybe<Object> none1 = Maybe.none();
    Maybe<Object> none2 = Maybe.none();
    assertTrue(none1.equals(none2));
    assertFalse(none1.equals(Maybe.maybe(42)));
  }

  @Test
  public void somesHaveEqualHashcodes() {
    Maybe<Integer> i1 = Maybe.maybe(42);
    Maybe<Integer> i2 = Maybe.maybe(42);
    Maybe<Integer> i3 = Maybe.maybe(7);
    assertTrue(i1.hashCode() == i2.hashCode());
    assertFalse(i1.hashCode() == i3.hashCode());
  }

  @Test
  public void nonesHaveEqualHashcodes() {
    Maybe<Object> none1 = Maybe.none();
    Maybe<Object> none2 = Maybe.none();
    assertTrue(none1.hashCode() == none2.hashCode());
  }
}
