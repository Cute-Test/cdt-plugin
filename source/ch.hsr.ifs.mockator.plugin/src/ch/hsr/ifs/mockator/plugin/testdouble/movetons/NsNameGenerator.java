package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;


class NsNameGenerator {

   public String getNsNameFor(final ICPPASTFunctionDefinition testFunction) {
      return withNsSuffix(getFunctionName(testFunction));
   }

   public String getNsNameFor(final ICPPASTCompositeTypeSpecifier testDouble) {
      return withNsSuffix(getTestDoubleName(testDouble));
   }

   private static String getTestDoubleName(final ICPPASTCompositeTypeSpecifier testDouble) {
      return testDouble.getName().toString().toLowerCase();
   }

   private static String getFunctionName(final ICPPASTFunctionDefinition testFunction) {
      return testFunction.getDeclarator().getName().toString();
   }

   private static String withNsSuffix(final String nsName) {
      return nsName + MockatorConstants.NS_SUFFIX;
   }
}
