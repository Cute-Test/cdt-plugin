package ch.hsr.ifs.mockator.tests;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.toIterable;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.model.ICProject;


public class FormatterOptionsLoader {
  private final ICProject cProject;

  public FormatterOptionsLoader(ICProject cProject) {
    this.cProject = cProject;
  }

  public void setFormatterOptions() {
    for (Entry<Object, Object> option : loadFormatterOptions().entrySet()) {
      cProject.setOption((String) option.getKey(), (String) option.getValue());
    }
  }

  private static Properties loadFormatterOptions() {
    Iterable<URL> mockatorFile =
        toIterable(MockatorTestPlugin.getDefault().getBundle()
            .findEntries("resources", "mockator_formatter.prefs", true));

    if (!mockatorFile.iterator().hasNext()) {
      fail("Mockator formatter preferences could not be found");
    }

    try {
      Properties formatterOptions = new Properties();
      formatterOptions.load(head(mockatorFile).get().openStream());
      return formatterOptions;
    } catch (IOException e) {
    }

    throw new AssertionFailedError("Problems occured while loading mockator formatter options");
  }
}
