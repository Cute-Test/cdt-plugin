package ch.hsr.ifs.mockator.tests.base.functional;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder;
import ch.hsr.ifs.mockator.plugin.base.functional.Injector;

public class HigherOrderTest {
  @Test
  public void filterWithPrimesUpTo100() {
    List<Integer> primesUpTo100 =
        list(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79,
            83, 89, 97);
    List<Integer> numbersUpToIncluding100 = list();
    for (int i = 0; i <= 100; i++) {
      numbersUpToIncluding100.add(i);
    }

    Collection<Integer> filtered =
        HigherOrder.filter(numbersUpToIncluding100, new F1<Integer, Boolean>() {
          @Override
          public Boolean apply(Integer number) {
            if (number <= 1)
              return false;

            for (int i = 2; i <= Math.sqrt(number); i++) {
              if (number % i == 0)
                return false;
            }
            return true;
          }
        });
    assertEquals(primesUpTo100, filtered);
  }

  @Test
  public void filterWithEmptyList() {
    List<Integer> numbers = list();
    Collection<Integer> allFiltered = HigherOrder.filter(numbers, new F1<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer number) {
        return true;
      }
    });
    assertEquals(list(), allFiltered);
  }

  @Test
  public void mapDoubleNumbers() {
    List<Integer> numbersUpto10 = list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    Collection<Integer> doubledNumbers = HigherOrder.map(numbersUpto10, new F1<Integer, Integer>() {
      @Override
      public Integer apply(Integer number) {
        return number * 2;
      }
    });
    List<Integer> expected = list(0, 2, 4, 6, 8, 10, 12, 14, 16, 18);
    assertEquals(expected, doubledNumbers);
  }

  @Test
  public void foldAverageOfNumbers() {
    final List<Integer> numbersUpToAndIncluding10 = list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    double result = HigherOrder.fold(numbersUpToAndIncluding10, new Injector<Integer, Double>() {
      double runningSum;

      @Override
      public void apply(Integer param) {
        runningSum += param;
      }

      @Override
      public Double yield() {
        double average = runningSum / numbersUpToAndIncluding10.size();
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
    Collection<F1V<Integer>> funs = list();
    F1V<Integer> fun = new F1V<Integer>() {
      @Override
      public void apply(Integer number) {
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
