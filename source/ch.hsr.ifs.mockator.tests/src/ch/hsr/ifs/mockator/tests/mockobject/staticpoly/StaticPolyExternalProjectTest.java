package ch.hsr.ifs.mockator.tests.mockobject.staticpoly;

import java.util.Properties;

import org.junit.Before;

public class StaticPolyExternalProjectTest extends StaticPolyMockObjectRefactoringTest {

  @Override
  protected void configureTest(Properties p) {
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
