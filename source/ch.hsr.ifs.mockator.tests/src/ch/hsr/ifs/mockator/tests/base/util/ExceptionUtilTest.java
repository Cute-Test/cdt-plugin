package ch.hsr.ifs.mockator.tests.base.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil;

public class ExceptionUtilTest {

   @Test
   public void singleExceptionHasNoRootCause() {
      final IllegalStateException rootCause = new IllegalStateException();

      ExceptionUtil.getRootCause(rootCause).ifPresent((ignored) -> assertTrue(false));
      //TODO remove unused code
      //    for (@SuppressWarnings("unused")
      //    Throwable optCause : ExceptionUtil.getRootCause(rootCause)) {
      //      assertTrue(false);
      //    }
   }

   @Test
   public void stackedExceptionsYieldsRootCause() {
      final IllegalStateException rootCause = new IllegalStateException();
      final MockatorException ex = new MockatorException(new IllegalArgumentException(rootCause));
      final Throwable cause = ExceptionUtil.getRootCause(ex).get();
      assertEquals(rootCause, cause);
   }
}
