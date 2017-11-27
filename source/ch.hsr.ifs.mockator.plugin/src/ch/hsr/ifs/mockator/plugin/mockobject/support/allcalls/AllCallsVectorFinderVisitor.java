package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;


public class AllCallsVectorFinderVisitor extends ASTVisitor {

   private IASTName registrationVector = null;

   {
      shouldVisitExpressions = true;
   }

   @Override
   public int visit(final IASTExpression expression) {
      if (!(expression instanceof IASTIdExpression)) {
         return PROCESS_CONTINUE;
      }

      final IASTIdExpression idExpr = (IASTIdExpression) expression;

      if (hasCallsVectorType(idExpr)) {
         registrationVector = idExpr.getName();
         return PROCESS_ABORT;
      }

      return PROCESS_CONTINUE;
   }

   private static boolean hasCallsVectorType(final IASTIdExpression idExpr) {
      final CallsVectorTypeVerifier verifier = new CallsVectorTypeVerifier(idExpr);
      return verifier.isVectorOfCallsVector();
   }

   public Optional<IASTName> getFoundCallsVector() {
      return Optional.ofNullable(registrationVector);
   }
}
