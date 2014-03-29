package ch.hsr.ifs.mockator.tests.base.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.util.HtmlUtil;

public class HtmlUtilTest {
  @Test
  public void escapeHtmlYieldsEscapedHtml() {
    String s = "This <text> has to be \"properly\" 'html' escaped & \\\n";
    String escaped = HtmlUtil.escapeHtml(s);
    String expected =
        "This &lt;text&gt; has to be &quot;properly&quot;"
            + " &#039;html&#039; escaped &amp; &#092;\n";
    assertEquals(expected, escaped);
  }
}
