package ch.hsr.ifs.cute.mockator.mockobject.registrations;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.MOCK_ID;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


public class MemberFunCallRegistrationAdder extends AbstractFunCallRegistrationAdder {

   private final boolean isStatic;
   private final String  nameOfAllCallsVector;

   public MemberFunCallRegistrationAdder(final ICPPASTFunctionDeclarator newFunDecl, final boolean isStatic, final CppStandard cppStd,
                                         final String nameOfAllCallsVector) {
      super(newFunDecl, cppStd);
      this.isStatic = isStatic;
      this.nameOfAllCallsVector = nameOfAllCallsVector;
   }

   @Override
   protected String getNameForCallsVector() {
      return nameOfAllCallsVector;
   }

   @Override
   protected IASTExpression getPushBackOwner() {
      final ICPPASTLiteralExpression arraySubscript = getArraySubscript();
      return nodeFactory.newArraySubscriptExpression(createCallSequence(), arraySubscript);
   }

   private ICPPASTLiteralExpression getArraySubscript() {
      if (isStatic) {
         return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0");
      } else {
         return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, MOCK_ID);
      }
   }

   @Override
   protected String getNameForCall() {
      return MockatorConstants.CALL;
   }
}
