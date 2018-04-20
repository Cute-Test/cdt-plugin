package ch.hsr.ifs.mockator.plugin.tests.linker.shadowfun;

public class ShadowFunctionRefProjectTest extends ShadowFunctionRefactoringTest {

   @Override
   protected void initReferencedProjects() throws Exception {
      stageReferencedProjectForBothProjects("ExternalFunction", "ExternalFunction.rts");
      super.initReferencedProjects();
   }

}
