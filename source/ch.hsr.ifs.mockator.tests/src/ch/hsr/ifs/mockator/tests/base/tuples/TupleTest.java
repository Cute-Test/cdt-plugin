package ch.hsr.ifs.mockator.tests.base.tuples;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedSet;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Singleton;
import ch.hsr.ifs.mockator.plugin.base.tuples.Triple;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;

public class TupleTest {

  @Test
  public void singletonCreation() {
    Set<Singleton<Integer>> integers = unorderedSet();
    Singleton<Integer> questionOfLive = Tuple.from(42);
    integers.add(questionOfLive);
    assertTrue(integers.remove(Tuple.from(42)));
    assertFalse(integers.contains(Tuple.from(42)));
  }

  @Test
  public void singletonToString() {
    Singleton<String> one = Tuple.from("One");
    String s = one.toString();
    assertEquals("(One)", s);
  }

  @Test
  public void singletonGet() {
    Singleton<Integer> t = Tuple.from(42);
    int i = _1(t);
    assertEquals(42, i);
  }

  @Test
  public void pairCreation() {
    Map<Pair<String, String>, Integer> marriage = unorderedMap();
    marriage.put(Tuple.from("Ursus", "Nadeschkin"), 12);
    marriage.put(Tuple.from("Bill", "Hillary"), 15);
    marriage.put(Tuple.from("Bill", "Melinda"), 22);

    int numberOfYears = marriage.get(Tuple.from("Bill", "Hillary"));
    assertEquals(15, numberOfYears);
    assertNull(marriage.get(Tuple.from("Charles", "Camilla")));
  }

  @Test
  public void pairToString() {
    Pair<String, String> couple = Tuple.from("Bill", "Hillary");
    String s = couple.toString();
    assertEquals("(Bill, Hillary)", s);
  }

  @Test
  public void pairGet() {
    Pair<String, Integer> t = Tuple.from("QuestionOfLive", 42);
    String s = _1(t);
    int i = _2(t);
    assertEquals("QuestionOfLive", s);
    assertEquals(42, i);
  }

  @Test
  public void tripleCreation() {
    Map<Triple<String, String, String>, Integer> numbers = unorderedMap();
    numbers.put(Tuple.from("One", "Two", "Three"), 2);
    numbers.put(Tuple.from("Four", "Five", "Six"), 5);
    numbers.put(Tuple.from("Seven", "Eight", "Nine"), 8);

    int middle = numbers.get(Tuple.from("Four", "Five", "Six"));
    assertEquals(5, middle);
    assertNull(numbers.get(Tuple.from("Eleven", "Twelve", "Thirteen")));
  }

  @Test
  public void tripleToString() {
    Triple<String, String, Integer> marriage = Tuple.from("Bill", "Hillary", 15);
    String s = marriage.toString();
    assertEquals("(Bill, Hillary, 15)", s);
  }

  @Test
  public void tripleGet() {
    Triple<String, String, Integer> marriage = Tuple.from("Bill", "Hillary", 15);
    String husband = _1(marriage);
    String spouse = _2(marriage);
    int numberOfYears = _3(marriage);
    assertEquals("Bill", husband);
    assertEquals("Hillary", spouse);
    assertEquals(15, numberOfYears);
  }
}
