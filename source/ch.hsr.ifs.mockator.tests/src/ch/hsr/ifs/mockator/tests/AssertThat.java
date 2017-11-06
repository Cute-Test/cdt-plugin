package ch.hsr.ifs.mockator.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.ui.tests.refactoring.TestHelper;

import junit.framework.ComparisonFailure;


public class AssertThat {

   private static Pattern WHITE_SPACES = Pattern.compile("\\s+");
   private final String   actual;

   public AssertThat(final String actual) {
      this.actual = unifyNewLines(actual);
   }

   public void isEqualByIgnoringWhitespace(String expected) {
      expected = unifyNewLines(expected);
      final String expectedIgnoringWs = WHITE_SPACES.matcher(expected).replaceAll("");
      final String actualIgnoringWs = WHITE_SPACES.matcher(actual).replaceAll("");

      if (!expectedIgnoringWs.equals(actualIgnoringWs)) throw new ComparisonFailure(null, expected, actual);
   }

   private static String unifyNewLines(final String code) {
      return TestHelper.unifyNewLines(code);
   }
}
