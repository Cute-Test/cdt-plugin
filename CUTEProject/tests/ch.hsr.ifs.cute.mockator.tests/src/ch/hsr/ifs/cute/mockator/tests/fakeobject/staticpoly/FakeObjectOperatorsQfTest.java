package ch.hsr.ifs.cute.mockator.tests.fakeobject.staticpoly;

import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;


public class FakeObjectOperatorsQfTest extends AbstractQuickfixTest {

   @Override
   protected IProblemId getProblemId() {
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
      return new FakeObjectQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "<b>13 member function(s) to implement</b>:<br/>operator -() const<br/>operator ++()<br/>operator ++(int)<br/>operator --()<br/>operator --(int)<br/>operator ()(const int&amp;)<br/>operator ==(const Fake&amp;) const<br/>operator +=(const Fake&amp;)<br/>operator /=(const Fake&amp;)<br/>operator !() const<br/>operator [](const int&amp;)<br/>operator &amp;()<br/>operator &lt;(const Fake&amp;) const";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Fake" };
   }
}
