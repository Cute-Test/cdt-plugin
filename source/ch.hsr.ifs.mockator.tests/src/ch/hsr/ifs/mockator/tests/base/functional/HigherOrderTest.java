package ch.hsr.ifs.mockator.tests.base.functional;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder;
import ch.hsr.ifs.mockator.plugin.base.functional.Injector;

public class HigherOrderTest {
  @Test
  public void filterWithPrimesUpTo100() {
    final List<Integer> primesUpTo100 = list(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97);
    final List<Integer> numbersUpToIncluding100 = list();
    for (int i = 0; i <= 100; i++) {
      numbersUpToIncluding100.add(i);
    }

    final Collection<Integer> filtered = HigherOrder.filter(numbersUpToIncluding100, (number) -> {
      if (number <= 1)
        return false;

      for (int i = 2; i <= Math.sqrt(number); i++) {
        if (number % i == 0)
          return false;
      }
      return true;
    });
    assertEquals(primesUpTo100, filtered);
  }

  @Test
  public void filterWithEmptyList() {
    final List<Integer> numbers = list();
    final Collection<Integer> allFiltered = HigherOrder.filter(numbers, (ignored) -> true);
    assertEquals(list(), allFiltered);
  }

  @Test
  public void mapDoubleNumbers() {
    final List<Integer> numbersUpto10 = list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final Collection<Integer> doubledNumbers = HigherOrder.map(numbersUpto10, (number) -> number * 2);
    final List<Integer> expected = list(0, 2, 4, 6, 8, 10, 12, 14, 16, 18);
    assertEquals(expected, doubledNumbers);
  }

  @Test
  public void foldAverageOfNumbers() {
    final List<Integer> numbersUpToAndIncluding10 = list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    final double result = HigherOrder.fold(numbersUpToAndIncluding10, new Injector<Integer, Double>() {
      double runningSum;

      @Override
      public void accept(final Integer param) {
        runningSum += param;
      }

      @Override
      public Double yield() {
        final double average = runningSum / numbersUpToAndIncluding10.size();
        return average;
      }
    });
    assertEquals(5.5, result, 0.0);
  }

  @Test
  public void forEachWithSideEffect() {
    class Counter {
      int i = 0;
    }
    final Counter counter = new Counter();
    final Collection<Consumer<Integer>> funs = list();
    final Consumer<Integer> fun = new Consumer<Integer>() {
      @Override
      public void accept(final Integer number) {
        counter.i += number;
      }
    };
    funs.add(fun);
    funs.add(fun);
    funs.add(fun);
    HigherOrder.forEach(funs, 1);
    assertEquals(3, counter.i);
  }
}
