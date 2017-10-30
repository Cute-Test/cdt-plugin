package ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.StdString;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;


abstract class AbstractExpectationsFinder {

   protected final NodeContainer<IASTName>     expectationVector;
   protected Collection<MemFunCallExpectation> callExpectations;
   private final IASTName                      expectationsName;

   public AbstractExpectationsFinder(Collection<MemFunCallExpectation> callExpectations, NodeContainer<IASTName> expectationVector,
                                     IASTName expectationsVectorName) {
      this.callExpectations = callExpectations;
      this.expectationVector = expectationVector;
      this.expectationsName = expectationsVectorName;
   }

   protected abstract void collectExpectations(IASTStatement expectationStmt);

   protected boolean matchesName(IASTName name) {
      return name.toString().equals(expectationsName.toString());
   }

   protected MemFunCallExpectation toMemberFunctionCall(IASTInitializerClause funSignature) {
      String signature = String.valueOf(((IASTLiteralExpression) funSignature).getValue());
      return new MemFunCallExpectation(signature);
   }

   protected boolean isStringLiteral(IASTInitializerClause literal) {
      return new StdString().isStdString(literal);
   }
}
