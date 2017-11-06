package ch.hsr.ifs.mockator.tests.base;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;


public class MockatorExceptionTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Test
   public void preservesExceptionMessage() {
      thrown.expect(MockatorException.class);
      thrown.expectMessage("Invalid XYZ");
      throw new MockatorException("Invalid XYZ");
   }

   @Test
   public void rethrowNestedExceptionWorks() throws Exception {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("Number not in range");

      try {
         throw new MockatorException(new IllegalArgumentException("Number not in range"));
      }
      catch (final MockatorException e1) {
         e1.rethrow();
      }
   }
}
