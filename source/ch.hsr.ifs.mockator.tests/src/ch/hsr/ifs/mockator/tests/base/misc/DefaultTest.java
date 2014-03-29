package ch.hsr.ifs.mockator.tests.base.misc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.misc.Default;

public class DefaultTest {

  @Test
  public void nullValueShouldYieldDefault() {
    assertEquals(Integer.valueOf(42), Default.whenNull(null, 42));
  }

  @Test
  public void nonNullValueShouldYieldGivenValue() {
    assertEquals(Integer.valueOf(3), Default.whenNull(3, 42));
  }
}
