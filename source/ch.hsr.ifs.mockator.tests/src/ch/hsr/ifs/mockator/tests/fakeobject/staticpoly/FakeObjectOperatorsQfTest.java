package ch.hsr.ifs.mockator.tests.fakeobject.staticpoly;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;


public class FakeObjectOperatorsQfTest extends MockatorQuickfixTest {

   @Override
   protected String getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL.getId();
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
   protected MockatorQuickFix getQuickfix() {
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
