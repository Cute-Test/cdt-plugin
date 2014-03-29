package ch.hsr.ifs.mockator.tests.base.util;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.toIterable;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.tests.MockatorTestPlugin;

public class FileUtilTest {

  @Test
  public void filePartRetrievalYieldsMockatorHeaderFile() {
    Enumeration<URL> files =
        MockatorTestPlugin.getDefault().getBundle()
            .findEntries("/externalTestResource/mockator", "*.h", false);
    String filePart = FileUtil.getFilePart(head(toIterable(files)).get().getFile());
    assertEquals("mockator.h", filePart);
  }

  @Test
  public void classicFileRetrieval() {
    assertEquals("mockator.h", FileUtil.getFilePart("/headers/mockator.h"));
  }

  @Test
  public void fileNameWithoutExtension() {
    assertEquals("mockator", FileUtil.getFilenameWithoutExtension("mockator.h"));
    assertEquals("mockator", FileUtil.getFilenameWithoutExtension("mockator"));
  }

  @Test
  public void removeFilePartYieldsDirectory() {
    assertEquals("/a/b/c/", FileUtil.removeFilePart("/a/b/c/mockator.h"));
    assertEquals("/a/b/c/", FileUtil.removeFilePart("/a/b/c/"));
  }
}
