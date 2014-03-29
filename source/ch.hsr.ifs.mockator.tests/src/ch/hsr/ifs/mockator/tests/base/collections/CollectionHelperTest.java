package ch.hsr.ifs.mockator.tests.base.collections;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingMap;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderedMap;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper;

public class CollectionHelperTest {

  @Test
  public void simpleArrayProperlyConstructed() {
    Integer[] array = CollectionHelper.array(1, 2, 3);
    assertNotNull(array);
    assertEquals(3, array.length);
    assertEquals(Integer.valueOf(1), array[0]);
    assertEquals(Integer.valueOf(2), array[1]);
    assertEquals(Integer.valueOf(3), array[2]);
  }

  @Test
  public void arrayFromListWithElements() {
    String[] expected = CollectionHelper.array("mockator", "is", "better", "than", "GoogleMock");
    List<String> list = CollectionHelper.list(expected);
    assertTrue(list instanceof ArrayList<?>);
    assertArrayEquals(expected, list.toArray(new String[list.size()]));
  }

  @Test
  public void emptyList() {
    List<String> list = CollectionHelper.list();
    assertNotNull(list);
    assertTrue(list.isEmpty());
    assertTrue(list instanceof ArrayList<?>);
  }

  @Test
  public void listFromCollection() {
    Collection<Integer> numbers = CollectionHelper.list(1, 2, 3);
    List<Integer> copied = CollectionHelper.list(numbers);
    assertEquals(numbers, copied);
  }

  @Test
  public void emptyUnorderedSet() {
    Set<String> set = CollectionHelper.unorderedSet();
    assertNotNull(set);
    assertTrue(set.isEmpty());
    assertTrue(set instanceof HashSet<?>);
  }

  @Test
  public void emptyOrderPreservingSet() {
    Set<String> set = CollectionHelper.orderPreservingSet();
    assertNotNull(set);
    assertTrue(set.isEmpty());
    assertTrue(set instanceof LinkedHashSet<?>);
  }

  @Test
  public void orderPreservingSetWithElementsFromArray() {
    String[] expected = CollectionHelper.array("mockator", "is", "better", "than", "GoogleMock");
    Set<String> set = CollectionHelper.orderPreservingSet(expected);
    assertTrue(set instanceof LinkedHashSet<?>);
    assertTrue(CollectionHelper.haveSameElementsInSameOrder(set, Arrays.asList(expected)));
  }

  @Test
  public void sameElementsInSameOrderYieldsTrue() {
    List<Integer> list = CollectionHelper.list(1, 5, 3, 7, 6, 9);
    Set<Integer> set = CollectionHelper.orderPreservingSet(1, 5, 3, 7, 6, 9);
    assertTrue(CollectionHelper.haveSameElementsInSameOrder(list, set));
  }

  @Test
  public void sameElementsInDifferentOrderYieldsFalse() {
    List<Integer> list = CollectionHelper.list(1, 5, 3, 7, 6, 9);
    Set<Integer> set = CollectionHelper.orderPreservingSet(1, 3, 5, 7, 6, 9);
    assertFalse(CollectionHelper.haveSameElementsInSameOrder(list, set));
  }

  @Test
  public void unorderedSetWithElementsFromArray() {
    String[] expected = CollectionHelper.array("mockator", "is", "better", "than", "GoogleMock");
    Set<String> set = CollectionHelper.unorderedSet(expected);
    assertTrue(set instanceof HashSet<?>);
    assertTrue(set.containsAll(Arrays.asList(expected)));
  }

  @Test
  public void emptyOrderPreservingMap() {
    Map<String, String> map = orderPreservingMap();
    assertNotNull(map);
    assertTrue(map.isEmpty());
    assertTrue(map instanceof LinkedHashMap<?, ?>);
  }

  @Test
  public void emptyUnorderedMap() {
    Map<String, String> map = unorderedMap();
    assertNotNull(map);
    assertTrue(map.isEmpty());
    assertTrue(map instanceof HashMap<?, ?>);
  }

  @Test
  public void emptyOrderedMap() {
    Map<String, String> map = orderedMap();
    assertNotNull(map);
    assertTrue(map.isEmpty());
    assertTrue(map instanceof TreeMap<?, ?>);
  }

  @Test
  public void zipMap() {
    Map<String, Integer> map =
        CollectionHelper.zipMap(array("one", "two", "three"), array(1, 2, 3));
    assertEquals(map.get("one"), Integer.valueOf(1));
    assertEquals(map.get("two"), Integer.valueOf(2));
    assertEquals(map.get("three"), Integer.valueOf(3));
  }

  @Test
  public void checkedCastWithTypeCompatibleObjects() {
    @SuppressWarnings("serial")
    List<Object> strings = new ArrayList<Object>() {
      {
        add("one");
        add("two");
        add("three");
      }
    };
    CollectionHelper.checkedCast(strings, String.class);
  }

  @Test(expected = ClassCastException.class)
  public void checkedCastWithUncompatibleObjects() {
    @SuppressWarnings("serial")
    List<Object> objects = new ArrayList<Object>() {
      {
        add("one");
        add(2);
        add("three");
      }
    };

    CollectionHelper.checkedCast(objects, String.class);
  }

  @Test
  public void iterableNoNullElements() {
    List<Integer> numbers = CollectionHelper.list(1, 2, 3, 4, 5);
    assertTrue(CollectionHelper.notNull(numbers));
  }

  @Test
  public void iterableWithOneNullElement() {
    List<Integer> numbers = CollectionHelper.list(1, 2, null, 4, 5);
    assertFalse(CollectionHelper.notNull(numbers));
  }

  @Test(expected = MockatorException.class)
  public void iterableWithNullListThrowsException() {
    CollectionHelper.notNull((Iterable<Object>) null);
  }

  @Test
  public void isEmptyIsTrueForEmptyList() {
    List<Integer> empty = CollectionHelper.list();
    assertTrue(CollectionHelper.isEmpty(empty));
  }

  @Test
  public void isEmptyIsFalseForNoneEmptyList() {
    List<Integer> numbers = CollectionHelper.list(1, 2, 3);
    assertFalse(CollectionHelper.isEmpty(numbers));
  }

  @Test
  public void getTailOfEmptyList() {
    assertEquals(CollectionHelper.list(), CollectionHelper.tail(CollectionHelper.<Integer>list()));
  }

  @Test
  public void getTailOfNonEmptyList() {
    List<Integer> numbers = CollectionHelper.list(1, 2, 3);
    assertEquals(CollectionHelper.list(2, 3), CollectionHelper.tail(numbers));
  }

  @Test
  public void getHeadOfNonEmptyList() {
    List<Integer> numbers = CollectionHelper.list(1, 2, 3);
    assertEquals(Integer.valueOf(1), CollectionHelper.head(numbers).get());
  }

  @Test
  public void getHeadOfEmptyListWithDefault() {
    List<Integer> numbers = CollectionHelper.list();
    assertEquals(Integer.valueOf(1), CollectionHelper.head(numbers, 1));
  }

  public void getHeadOfEmptyList() {
    List<Integer> numbers = CollectionHelper.list();
    assertTrue(CollectionHelper.head(numbers).isNone());
  }

  @Test
  public void getLastOfNonEmptyList() {
    List<Integer> numbers = CollectionHelper.list(1, 2, 3);
    assertEquals(Integer.valueOf(3), CollectionHelper.last(numbers).get());
  }

  public void getLastOfEmptyList() {
    List<Integer> numbers = CollectionHelper.list();
    assertTrue(CollectionHelper.last(numbers).isNone());
  }

  @Test
  public void toIterableWithEnumeration() {
    Vector<Integer> v = new Vector<Integer>(CollectionHelper.list(1, 2, 3));
    Iterable<Integer> iterable = CollectionHelper.toIterable(v.elements());
    assertEquals(Integer.valueOf(1), iterable.iterator().next());
    assertEquals(Integer.valueOf(2), iterable.iterator().next());
    assertEquals(Integer.valueOf(3), iterable.iterator().next());
    assertFalse(iterable.iterator().hasNext());
  }
}
