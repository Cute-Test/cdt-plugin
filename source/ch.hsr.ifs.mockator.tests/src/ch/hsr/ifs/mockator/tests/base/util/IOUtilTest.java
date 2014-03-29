package ch.hsr.ifs.mockator.tests.base.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.util.IOUtil;

public class IOUtilTest {
  private static final String TMP_FILE_NAME = "tmp.txt";

  @Test
  public void safeCloseWithNullShouldNotYieldException() {
    IOUtil.safeClose(null);
  }

  @Test(expected = IOException.class)
  public void safeCloseWithWriteAfterCloseYieldsIOException() throws IOException {
    try {
      DataOutputStream os = new DataOutputStream(new FileOutputStream(TMP_FILE_NAME));
      IOUtil.safeClose(os);
      os.write(0);
    } finally {
      boolean success = new File(TMP_FILE_NAME).delete();
      assertTrue(success);
    }
  }

  @Test
  public void stringToStreamYieldsSameString() throws IOException {
    String text = "Mockator";
    InputStream stringToStream = IOUtil.stringToStream(text);
    StringBuilder sb = new StringBuilder();

    int c;
    while ((c = stringToStream.read()) != -1) {
      sb.append((char) c);
    }

    assertEquals(text, sb.toString());
  }
}
