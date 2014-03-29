package ch.hsr.ifs.mockator.tests.base.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil;

public class ExceptionUtilTest {

  @Test
  public void singleExceptionHasNoRootCause() {
    IllegalStateException rootCause = new IllegalStateException();

    for (@SuppressWarnings("unused")
    Throwable optCause : ExceptionUtil.getRootCause(rootCause)) {
      assertTrue(false);
    }
  }

  @Test
  public void stackedExceptionsYieldsRootCause() {
    IllegalStateException rootCause = new IllegalStateException();
    MockatorException ex = new MockatorException(new IllegalArgumentException(rootCause));
    Throwable cause = ExceptionUtil.getRootCause(ex).get();
    assertEquals(rootCause, cause);
  }
}
