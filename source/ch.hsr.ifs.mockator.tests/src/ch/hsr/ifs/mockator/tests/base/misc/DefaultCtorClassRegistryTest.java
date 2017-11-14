package ch.hsr.ifs.mockator.tests.base.misc;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper;
import ch.hsr.ifs.mockator.plugin.base.misc.DefaultCtorClassRegistry;


public class DefaultCtorClassRegistryTest {

   @Test
   public void classesWithAllDefaultCtors() {
      final Set<Class<? extends Vehicle>> klasses = orderPreservingSet(Car.class, Plane.class);
      assertEquals(CollectionHelper.list(new Car(), new Plane()), new DefaultCtorClassRegistry<>(klasses).createInstances());
   }

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Test
   public void oneNonDefaultCtorYieldsException() {
      thrown.expect(ILTISException.class);
      thrown.expectMessage("Class has no default constructor: Bicycle");
      final Set<Class<? extends Vehicle>> classes = orderPreservingSet(Car.class, Plane.class, Bicycle.class);
      new DefaultCtorClassRegistry<>(classes).createInstances();
   }

   @Test
   public void oneNotAccessibleClassYieldsException() {
      thrown.expect(ILTISException.class);
      thrown.expectMessage("No access to class Bike");
      final Set<Class<? extends Vehicle>> classes = orderPreservingSet(Car.class, Plane.class, Bike.class);
      new DefaultCtorClassRegistry<>(classes).createInstances();
   }

   public static class Vehicle {}

   public static class Car extends Vehicle {

      @Override
      public boolean equals(final Object o) {
         return true;
      }

      @Override
      public int hashCode() {
         return 3;
      }
   }

   public static class Plane extends Vehicle {

      @Override
      public boolean equals(final Object o) {
         return true;
      }

      @Override
      public int hashCode() {
         return 5;
      }
   }

   public static class Bicycle extends Vehicle {

      public Bicycle(final int i) {}
   }

   private static class Bike extends Vehicle {}
}
