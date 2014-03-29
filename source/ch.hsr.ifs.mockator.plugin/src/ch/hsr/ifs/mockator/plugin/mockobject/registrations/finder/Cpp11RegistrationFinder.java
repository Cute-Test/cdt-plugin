package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;

public class Cpp11RegistrationFinder extends RegistrationFinder {

  @Override
  protected ExistingMemFunCallRegistration collectRegistration(IASTInitializerClause pushBackArg) {
    ICPPASTSimpleTypeConstructorExpression typeCtor = getCallTypeCtor(pushBackArg);
    IASTInitializerClause funSignature = getFunSignatureClause(typeCtor);
    return toExistingCallRegistration(funSignature, getContainingStmt(pushBackArg));
  }

  private IASTInitializerClause getFunSignatureClause(ICPPASTSimpleTypeConstructorExpression ctor) {
    IASTInitializer ctorInitializer = ctor.getInitializer();
    Assert.instanceOf(ctorInitializer, ICPPASTInitializerList.class, "Initializer list expected");
    IASTInitializerClause[] ctorClauses = ((ICPPASTInitializerList) ctorInitializer).getClauses();
    Assert.isTrue(ctorClauses.length > 0, "Empty call ctor not allowed");
    IASTInitializerClause funSignature = ctorClauses[0];
    Assert.isTrue(isStringLiteral(funSignature), "Fun signature must be a string literal");
    return funSignature;
  }

  private void assureIsCallType(ICPPASTSimpleTypeConstructorExpression typeCtor) {
    ICPPASTDeclSpecifier declSpecifier = typeCtor.getDeclSpecifier();
    Assert.isTrue(isCall(declSpecifier), "Not of call type");
  }

  private ICPPASTSimpleTypeConstructorExpression getCallTypeCtor(IASTInitializerClause pushBackArg) {
    Assert.instanceOf(pushBackArg, ICPPASTSimpleTypeConstructorExpression.class,
        "Wrong push_back argument: " + pushBackArg.getClass().getName());
    ICPPASTSimpleTypeConstructorExpression typeCtor =
        (ICPPASTSimpleTypeConstructorExpression) pushBackArg;
    assureIsCallType(typeCtor);
    return typeCtor;
  }

  private boolean isCall(ICPPASTDeclSpecifier declSpecifier) {
    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return false;

    return isNameCall(((ICPPASTNamedTypeSpecifier) declSpecifier).getName());
  }
}
