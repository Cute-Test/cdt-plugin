package ch.hsr.ifs.mockator.plugin.tests.mockobject.subtype;

import java.util.Properties;

import org.junit.Before;


public class SubTypeExternalProjectTest extends SubTypeMockObjectRefactoringTest {

   @Override
   protected void configureTest(final Properties p) {
      super.configureTest(p);
      withCuteNature = true;
   }

   @Override
   @Before
   public void setUp() throws Exception {
      addReferencedProject("SUTProject", "SUTProject.rts");
      super.setUp();
   }
}
