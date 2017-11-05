package ch.hsr.ifs.mockator.tests.base.tuples;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;

public class TupleTest {

  @Test
  public void pairCreation() {
    final Map<Pair<String, String>, Integer> marriage = unorderedMap();
    marriage.put(new Pair<>("Ursus", "Nadeschkin"), 12);
    marriage.put(new Pair<>("Bill", "Hillary"), 15);
    marriage.put(new Pair<>("Bill", "Melinda"), 22);

    final int numberOfYears = marriage.get(new Pair<>("Bill", "Hillary"));
    assertEquals(15, numberOfYears);
    assertNull(marriage.get(new Pair<>("Charles", "Camilla")));
  }

  @Test
  public void pairToString() {
    final Pair<String, String> couple = new Pair<>("Bill", "Hillary");
    final String s = couple.toString();
    assertEquals("(Bill, Hillary)", s);
  }

  @Test
  public void pairGet() {
    final Pair<String, Integer> t = new Pair<>("QuestionOfLive", 42);
    final String s = t.first();
    final int i = t.second();
    assertEquals("QuestionOfLive", s);
    assertEquals(42, i);
  }

  @Test
  public void tripleToString() {
    final Pair<String, Pair<String, Integer>> marriage = new Pair<>("Bill", new Pair<>("Hillary", 15));
    final String s = marriage.toString();
    assertEquals("(Bill, Hillary, 15)", s);
  }

}
