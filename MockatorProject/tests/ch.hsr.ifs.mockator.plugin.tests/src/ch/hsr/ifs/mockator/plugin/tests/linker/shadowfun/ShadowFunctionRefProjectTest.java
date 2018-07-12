package ch.hsr.ifs.mockator.plugin.tests.linker.shadowfun;

import org.junit.Ignore;

public class ShadowFunctionRefProjectTest extends ShadowFunctionRefactoringTest {

   @Ignore
   @Override
   protected void initReferencedProjects() throws Exception {
      stageReferencedProjectForBothProjects("ExternalFunction", "ExternalFunction.rts");
      super.initReferencedProjects();
   }

}
