package ch.hsr.ifs.mockator.tests.mockobject.subtype;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.mockobject.qf.MockObjectByFunsQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.AbstractQuickfixTest;


public class MockObjectCpp11SubTypeQfTest extends AbstractQuickfixTest {

   @Override
   protected String getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL.getId();
   }

   @Override
   protected CppStandard getCppStdToUse() {
      return CppStandard.Cpp11Std;
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
      return new MockObjectByFunsQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "<b>3 member function(s) to implement</b>:<br/>Mock()<br/>base()<br/>foo()";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Mock" };
   }
}
