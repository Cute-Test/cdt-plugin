package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public class Cpp11RegistrationFinder extends RegistrationFinder {

   @Override
   protected ExistingMemFunCallRegistration collectRegistration(final IASTInitializerClause pushBackArg) {
      final ICPPASTSimpleTypeConstructorExpression typeCtor = getCallTypeCtor(pushBackArg);
      final IASTInitializerClause funSignature = getFunSignatureClause(typeCtor);
      return toExistingCallRegistration(funSignature, getContainingStmt(pushBackArg));
   }

   private IASTInitializerClause getFunSignatureClause(final ICPPASTSimpleTypeConstructorExpression ctor) {
      final IASTInitializer ctorInitializer = ctor.getInitializer();
      ILTISException.Unless.assignableFrom("Initializer list expected", ICPPASTInitializerList.class, ctorInitializer);
      final IASTInitializerClause[] ctorClauses = ((ICPPASTInitializerList) ctorInitializer).getClauses();
      ILTISException.Unless.isTrue("Empty call ctor not allowed", ctorClauses.length > 0);
      final IASTInitializerClause funSignature = ctorClauses[0];
      ILTISException.Unless.isTrue("Fun signature must be a string literal", isStringLiteral(funSignature));
      return funSignature;
   }

   private void assureIsCallType(final ICPPASTSimpleTypeConstructorExpression typeCtor) {
      final ICPPASTDeclSpecifier declSpecifier = typeCtor.getDeclSpecifier();
      ILTISException.Unless.isTrue("Not of call type", isCall(declSpecifier));
   }

   private ICPPASTSimpleTypeConstructorExpression getCallTypeCtor(final IASTInitializerClause pushBackArg) {
      ILTISException.Unless.assignableFrom("Wrong push_back argument: " + pushBackArg.getClass().getName(),
            ICPPASTSimpleTypeConstructorExpression.class, pushBackArg);
      final ICPPASTSimpleTypeConstructorExpression typeCtor = (ICPPASTSimpleTypeConstructorExpression) pushBackArg;
      assureIsCallType(typeCtor);
      return typeCtor;
   }

   private boolean isCall(final ICPPASTDeclSpecifier declSpecifier) {
      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) return false;

      return isNameCall(((ICPPASTNamedTypeSpecifier) declSpecifier).getName());
   }
}
