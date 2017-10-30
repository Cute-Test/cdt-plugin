package ch.hsr.ifs.mockator.plugin.mockobject.expectations;

import org.eclipse.cdt.core.dom.ast.IASTName;


public class ExpectedNameCreator {

   private static final String EXPECTED = "expected";
   private final String        testDoubleName;

   public ExpectedNameCreator(IASTName testDoubleName) {
      this.testDoubleName = testDoubleName.toString();
   }

   public ExpectedNameCreator(String testDoubleName) {
      this.testDoubleName = testDoubleName;
   }

   public String getNameForExpectationsVector() {
      return EXPECTED + testDoubleName;
   }
}
