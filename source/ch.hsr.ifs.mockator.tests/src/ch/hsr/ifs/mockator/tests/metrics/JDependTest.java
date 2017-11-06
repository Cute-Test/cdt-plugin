package ch.hsr.ifs.mockator.tests.metrics;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.checkedCast;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;


// Taken and adapted from http://clarkware.com/software/JDepend.html#junit
public class JDependTest {

   private JDepend                 jdepend;
   private Collection<JavaPackage> packages;

   @Before
   public void setUp() throws IOException {
      initJDepend();
      startAnalysis();
   }

   private void initJDepend() throws IOException {
      final PackageFilter filter = createPackageFilter();
      jdepend = new JDepend(filter);
      jdepend.addDirectory(getBinaryDirectory());
      handleNotVolatilePackages();
   }

   private static PackageFilter createPackageFilter() {
      final PackageFilter filter = new PackageFilter();
      filter.addPackage("java.*");
      filter.addPackage("javax.*");
      filter.addPackage("org.*");
      filter.addPackage("ch.hsr.ifs.cute.*");
      filter.addPackage("ch.hsr.ifs.mockator.plugin.incompleteclass.checker"); // FIXME try to get rid of this one
      filter.addPackage("ch.hsr.ifs.cdt.compatibility.changes"); //FIXME this package will vanish with future CDT releases
      return filter;
   }

   private void handleNotVolatilePackages() {
      // Packages that are not expected to change can be specifically
      // configured with a volatility (V) value.
      // V can either be 0 or 1. If V=0, meaning the package is not at all
      // subject to change, then the package will automatically fall
      // directly on the main sequence (D=0). The following
      // packages are not volatile and maximally stable. Creating
      // dependencies on them is therefore no concern.

      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.base");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.base.tuples");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.base.maybe");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.base.collections");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.finder");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.functions");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.tu");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.utils");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.functions.params");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.refsupport.includes");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.mockobject.support.context");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.extractinterface.context");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.testdouble.creation");
      addNotVolatilePackage("ch.hsr.ifs.mockator.plugin.testdouble.qf");
   }

   private void addNotVolatilePackage(final String packageName) {
      final JavaPackage javaPackage = new JavaPackage(packageName);
      javaPackage.setVolatility(0);
      jdepend.addPackage(javaPackage);
   }

   private void startAnalysis() {
      packages = checkedCast(jdepend.analyze(), JavaPackage.class);
   }

   @Test
   public void noCyclicPackageDependencies() throws Exception {
      final StringBuilder cycles = new StringBuilder();

      for (final JavaPackage p : packages) {
         final List<JavaPackage> packages = list();

         if (p.collectCycle(packages)) {
            cycles.append(String.format("%n$ %s $ [", p.getName()));

            for (int i = 0; i < packages.size(); i++) {
               if (i > 0) {
                  cycles.append(" -> ");
               }

               cycles.append(packages.get(i).getName());
            }

            cycles.append("]");
         }
      }

      assertEquals("Cycles exist in packages: " + cycles.toString(), false, jdepend.containsCycles());
   }

   private static String getBinaryDirectory() {
      try {
         // Locally we should take bin directory because Eclipse keeps this
         // always up-to-date; on the build server only the target/classes directory
         // exists because Maven stores the class files there
         final File eclipseBinDir = getEclipseBinaryDir();

         if (eclipseBinDir.exists()) { return eclipseBinDir.getPath(); }
      }
      catch (final Exception e) {
         return getMavenTargetDir();
      }

      throw new IllegalStateException("Problems determining binary directory for metric tests");
   }

   private static String getMavenTargetDir() {
      return "../ch.hsr.ifs.mockator.plugin/target/classes";
   }

   private static File getEclipseBinaryDir() throws URISyntaxException, MalformedURLException {
      final String relPathToMockatorPlugin = "../../../../../../../../ch.hsr.ifs.mockator.plugin/";
      final URL currentDir = JDependTest.class.getResource("./");
      return new File(new URL(currentDir, relPathToMockatorPlugin + "bin").toURI());
   }
}
