package ch.hsr.ifs.cute.mockator.tests.fakeobject.subtype;

import ch.hsr.ifs.cute.mockator.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class FakeObjectSubTypeQfTest extends AbstractQuickfixTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
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
        return new FakeObjectQuickFix();
    }

    @Override
    protected String getResolutionMessage() {
        return "<b>4 member function(s) to implement</b>:<br/>Fake()<br/>base()<br/>foo()<br/>operator ++()";
    }

    @Override
    protected String[] getMarkerMessages() {
        return new String[] { "Necessary member function(s) not existing in class Fake" };
    }
}
