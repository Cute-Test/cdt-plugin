package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.StdString;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

abstract class RegistrationFinder {

  public Maybe<ExistingMemFunCallRegistration> findRegistration(IASTName callsVectorUsage) {
    if (!AstUtil.isPushBack(callsVectorUsage) || !isArrayAccess(callsVectorUsage))
      return none();

    ICPPASTFunctionCallExpression funCall =
        AstUtil.getAncestorOfType(callsVectorUsage, ICPPASTFunctionCallExpression.class);

    if (funCall == null || funCall.getArguments().length != 1)
      return none();

    IASTInitializerClause call = funCall.getArguments()[0];
    return maybe(collectRegistration(call));
  }

  protected abstract ExistingMemFunCallRegistration collectRegistration(
      IASTInitializerClause pushBackArg);

  private static boolean isArrayAccess(IASTName callsVector) {
    return AstUtil.getAncestorOfType(callsVector, ICPPASTArraySubscriptExpression.class) != null;
  }

  protected ExistingMemFunCallRegistration toExistingCallRegistration(
      IASTInitializerClause funSignature, IASTStatement containingStmt) {
    String signature = String.valueOf(((IASTLiteralExpression) funSignature).getValue());
    ICPPASTFunctionDefinition parent =
        AstUtil.getAncestorOfType(containingStmt, ICPPASTFunctionDefinition.class);
    ExistingTestDoubleMemFun memFun = new ExistingTestDoubleMemFun(parent);
    return new ExistingMemFunCallRegistration(memFun, containingStmt, signature);
  }

  protected boolean isNameCall(IASTName name) {
    return name.toString().equals(MockatorConstants.CALL);
  }

  protected boolean isStringLiteral(IASTInitializerClause param) {
    return new StdString().isStdString(param);
  }

  protected IASTStatement getContainingStmt(IASTInitializerClause param) {
    return AstUtil.getAncestorOfType(param, IASTStatement.class);
  }
}
