package ch.hsr.ifs.cute.mockator.tests.mockobject.staticpoly;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.mockobject.qf.MockObjectByFunsQuickFix;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class MockObjectCpp03StaticPolyQfTest extends AbstractQuickfixTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
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
        return new MockObjectByFunsQuickFix();
    }

    @Override
    protected String getResolutionMessage() {
        return "<b>2 member function(s) to implement</b>:<br/>Mock()<br/>foo() const";
    }

    @Override
    protected String[] getMarkerMessages() {
        return new String[] { "Necessary member function(s) not existing in class Mock" };
    }
}
