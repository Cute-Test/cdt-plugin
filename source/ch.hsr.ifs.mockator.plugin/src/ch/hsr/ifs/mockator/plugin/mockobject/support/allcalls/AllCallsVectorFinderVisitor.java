package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.ui.refactoring.Container;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

@SuppressWarnings("restriction")
public class AllCallsVectorFinderVisitor extends ASTVisitor {
  private final Container<IASTName> registrationVector;

  {
    shouldVisitExpressions = true;
  }

  public AllCallsVectorFinderVisitor() {
    registrationVector = new Container<IASTName>();
  }

  @Override
  public int visit(IASTExpression expression) {
    if (!(expression instanceof IASTIdExpression))
      return PROCESS_CONTINUE;

    IASTIdExpression idExpr = (IASTIdExpression) expression;

    if (hasCallsVectorType(idExpr)) {
      registrationVector.setObject(idExpr.getName());
      return PROCESS_ABORT;
    }

    return PROCESS_CONTINUE;
  }

  private static boolean hasCallsVectorType(IASTIdExpression idExpr) {
    CallsVectorTypeVerifier verifier = new CallsVectorTypeVerifier(idExpr);
    return verifier.isVectorOfCallsVector();
  }

  public Maybe<IASTName> getFoundCallsVector() {
    return maybe(registrationVector.getObject());
  }
}
