package ch.hsr.ifs.mockator.plugin.tests.base.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.iltis.core.plugin.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil;


public class ExceptionUtilTest {

   @Test
   public void singleExceptionHasNoRootCause() {
      final IllegalStateException rootCause = new IllegalStateException();
      ExceptionUtil.getRootCause(rootCause).ifPresent((ignored) -> assertTrue(false));
   }

   @Test
   public void stackedExceptionsYieldsRootCause() {
      final IllegalStateException rootCause = new IllegalStateException();
      final ILTISException ex = new ILTISException(new IllegalArgumentException(rootCause));
      final Throwable cause = ExceptionUtil.getRootCause(ex).get();
      assertEquals(rootCause, cause);
   }
}
