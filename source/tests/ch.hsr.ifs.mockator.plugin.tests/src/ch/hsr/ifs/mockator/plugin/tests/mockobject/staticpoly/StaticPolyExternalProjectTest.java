package ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly;

import java.util.Properties;


public class StaticPolyExternalProjectTest extends StaticPolyMockObjectRefactoringTest {

   @Override
   protected void configureTest(final Properties p) {
      super.configureTest(p);
      withCuteNature = true;
   }

   @Override
   protected void initReferencedProjects() throws Exception {
      stageReferencedProjectForBothProjects("SUTProject", "SUTProject.rts");
      super.initReferencedProjects();
   }

}
