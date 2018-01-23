package ch.hsr.ifs.mockator.tests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.core.functional.StreamHelper;

import junit.framework.AssertionFailedError;


public class FormatterOptionsLoader {

   private final ICProject cProject;

   public FormatterOptionsLoader(final ICProject cProject) {
      this.cProject = cProject;
   }

   public void setFormatterOptions() {
      for (final Entry<Object, Object> option : loadFormatterOptions().entrySet()) {
         cProject.setOption((String) option.getKey(), (String) option.getValue());
      }
   }

   private static Properties loadFormatterOptions() {
      final Stream<URL> mockatorFile = StreamHelper.from(MockatorTestPlugin.getDefault().getBundle().findEntries("resources",
            "mockator_formatter.prefs", true));

      final Optional<URL> first = mockatorFile.findFirst();
      
      if (!first.isPresent()) {
         fail("Mockator formatter preferences could not be found");
      }

      try {
         final Properties formatterOptions = new Properties();
         formatterOptions.load(first.get().openStream());
         return formatterOptions;
      }
      catch (final IOException e) {}

      throw new AssertionFailedError("Problems occured while loading mockator formatter options");
   }
}
