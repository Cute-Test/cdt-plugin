package ch.hsr.ifs.mockator.tests;

import java.util.regex.Pattern;

import junit.framework.ComparisonFailure;

import org.eclipse.cdt.ui.tests.refactoring.TestHelper;

public class AssertThat {
  private static Pattern WHITE_SPACES = Pattern.compile("\\s+");
  private final String actual;

  public AssertThat(String actual) {
    this.actual = unifyNewLines(actual);
  }

  public void isEqualByIgnoringWhitespace(String expected) {
    expected = unifyNewLines(expected);
    String expectedIgnoringWs = WHITE_SPACES.matcher(expected).replaceAll("");
    String actualIgnoringWs = WHITE_SPACES.matcher(actual).replaceAll("");

    if (!expectedIgnoringWs.equals(actualIgnoringWs))
      throw new ComparisonFailure(null, expected, actual);
  }

  private static String unifyNewLines(String code) {
    return TestHelper.unifyNewLines(code);
  }
}
