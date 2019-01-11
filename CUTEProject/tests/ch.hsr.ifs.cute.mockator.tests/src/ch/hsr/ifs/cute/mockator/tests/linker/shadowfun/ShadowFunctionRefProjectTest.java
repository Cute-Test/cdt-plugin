package ch.hsr.ifs.cute.mockator.tests.linker.shadowfun;

import org.junit.Ignore;


@Ignore
public class ShadowFunctionRefProjectTest extends ShadowFunctionRefactoringTest {

    @Override
    protected void initReferencedProjects() throws Exception {
        stageReferencedProjectForBothProjects("ExternalFunction", "ExternalFunction.rts");
        super.initReferencedProjects();
    }

}
