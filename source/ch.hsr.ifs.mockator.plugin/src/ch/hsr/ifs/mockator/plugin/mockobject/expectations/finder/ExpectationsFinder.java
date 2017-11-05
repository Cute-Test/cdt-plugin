package ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

public class ExpectationsFinder {

  private final Collection<MemFunCallExpectation> callExpectations;
  private final NodeContainer<IASTName> expectationVector;
  private final IASTFunctionDefinition testFunction;

  public ExpectationsFinder(final IASTFunctionDefinition testFunction) {
    this.testFunction = testFunction;
    callExpectations = orderPreservingSet();
    expectationVector = new NodeContainer<>();
  }

  public Pair<Collection<MemFunCallExpectation>, IASTName> getExpectations(final IASTName assertedExpectetation) {
    testFunction.accept(new ASTVisitor() {

      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(final IASTName name) {
        if (!nameMatches(name)) {
          return PROCESS_SKIP;
        }

        return collectExpectations(name);
      }

      private boolean nameMatches(final IASTName name) {
        return name.toString().equals(assertedExpectetation.toString());
      }

      private int collectExpectations(final IASTName name) {
        final IASTStatement stmt = AstUtil.getAncestorOfType(name, IASTStatement.class);

        if (stmt instanceof IASTDeclarationStatement) {
          new InitializerExpectationsFinder(callExpectations, expectationVector, assertedExpectetation).collectExpectations(stmt);
        } else if (stmt instanceof IASTExpressionStatement) {
          new BoostVectorExpectationsFinder(callExpectations, expectationVector, assertedExpectetation).collectExpectations(stmt);
        }

        if (callExpectations.isEmpty()) {
          return PROCESS_CONTINUE;
        }

        return PROCESS_ABORT;
      }
    });

    return Pair.from(callExpectations, getNameOfExpectationVector(assertedExpectetation));
  }

  private IASTName getNameOfExpectationVector(final IASTName assertedExpectation) {
    return OptHelper.returnIfPresentElse(expectationVector.getNode(), (node) -> node, () -> {
      final IBinding binding = assertedExpectation.resolveBinding();
      final IASTName[] definitions = assertedExpectation.getTranslationUnit().getDefinitionsInAST(binding);
      Assert.isTrue(definitions.length > 0, "Expectation vector must have a definition");
      return definitions[0];
    });
  }
}
