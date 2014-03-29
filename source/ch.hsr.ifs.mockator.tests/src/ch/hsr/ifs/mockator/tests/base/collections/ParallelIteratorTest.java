package ch.hsr.ifs.mockator.tests.base.collections;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.collections.ParallelIterator;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;

public class ParallelIteratorTest {

  @Test
  public void nextGivesExpectedValues() {
    List<Integer> l1 = list(1, 3);
    List<Integer> l2 = list(2, 4);
    ParallelIterator<Integer, Integer> p = createParallelIt(l1, l2);
    Pair<Integer, Integer> next = p.next();
    assertEquals(new Pair<Integer, Integer>(1, 2), next);
    next = p.next();
    assertEquals(new Pair<Integer, Integer>(3, 4), next);
    assertFalse(p.hasNext());
  }

  @Test
  public void oneEmptyCollectionYieldsEndOfIterator() {
    List<Integer> list1 = list(1, 3, 5);
    List<Integer> list2 = list();
    assertFalse(createParallelIt(list1, list2).hasNext());
  }

  @Test
  public void shorterCollectionEndsIteration() {
    List<Integer> list1 = list(1, 3, 5);
    List<Integer> list2 = list(1, 3);
    ParallelIterator<Integer, Integer> p = createParallelIt(list1, list2);
    assertTrue(p.hasNext());
    p.next();
    assertTrue(p.hasNext());
    p.next();
    assertFalse(p.hasNext());
  }

  @Test
  public void removeWorksAsExpected() {
    List<Integer> list1 = list(1, 3);
    List<Integer> list2 = list(2, 3, 4);
    ParallelIterator<Integer, Integer> p = createParallelIt(list1, list2);
    p.next();
    p.remove();
    p.next();
    p.remove();
    assertTrue(list1.isEmpty());
    assertEquals(list(4), list2);
  }

  private static ParallelIterator<Integer, Integer> createParallelIt(List<Integer> list1,
      List<Integer> list2) {
    return new ParallelIterator<Integer, Integer>(list1.iterator(), list2.iterator());
  }
}
