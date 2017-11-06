package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.incompleteclass.subtype.SubtypePolymorphismChecker;


public class OnlyPointerAndReferenceDeclarationTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return SubtypePolymorphismChecker.SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
