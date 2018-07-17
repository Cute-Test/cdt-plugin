package ch.hsr.ifs.cute.mockator.mockobject.expectations.finder;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.mockator.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.StdString;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


abstract class AbstractExpectationsFinder {

   protected final NodeContainer<IASTName>     expectationVector;
   protected Collection<MemFunCallExpectation> callExpectations;
   private final IASTName                      expectationsName;

   public AbstractExpectationsFinder(final Collection<MemFunCallExpectation> callExpectations, final NodeContainer<IASTName> expectationVector,
                                     final IASTName expectationsVectorName) {
      this.callExpectations = callExpectations;
      this.expectationVector = expectationVector;
      expectationsName = expectationsVectorName;
   }

   protected abstract void collectExpectations(IASTStatement expectationStmt);

   protected boolean matchesName(final IASTName name) {
      return name.toString().equals(expectationsName.toString());
   }

   protected MemFunCallExpectation toMemberFunctionCall(final IASTInitializerClause funSignature) {
      final String signature = String.valueOf(((IASTLiteralExpression) funSignature).getValue());
      return new MemFunCallExpectation(signature);
   }

   protected boolean isStringLiteral(final IASTInitializerClause literal) {
      return new StdString().isStdString(literal);
   }
}
