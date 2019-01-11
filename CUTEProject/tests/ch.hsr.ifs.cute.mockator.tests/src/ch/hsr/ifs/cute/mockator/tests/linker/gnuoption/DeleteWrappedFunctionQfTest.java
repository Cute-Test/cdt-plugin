package ch.hsr.ifs.cute.mockator.tests.linker.gnuoption;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf.DeleteWrappedFunctionQuickFix;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class DeleteWrappedFunctionQfTest extends AbstractQuickfixTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.WRAP_FUNCTION;
    }

    @Override
    protected CppStandard getCppStdToUse() {
        return CppStandard.Cpp03Std;
    }

    @Override
    protected boolean isManagedBuildProjectNecessary() {
        return true;
    }

    @Override
    protected boolean isRefactoringUsed() {
        return true;
    }

    @Override
    protected MockatorQuickFix createMarkerResolution() {
        return new DeleteWrappedFunctionQuickFix();
    }

    @Override
    protected String getResolutionMessage() {
        return "Delete wrapped function";
    }

    @Override
    protected String[] getMarkerMessages() {
        return null; // new String[] {"Manage wrapped function \"_Z3foov\""};
    }
}
