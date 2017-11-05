package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorFinderVisitor;

public class AssertEqualFinderVisitor extends ASTVisitor {

  private final Optional<ICPPASTCompositeTypeSpecifier> mockObject;
  private final Optional<IASTName> registrationVector;
  private final List<ExpectedActualPair> expectedActualPairs;

  {
    shouldVisitStatements = true;
  }

  public AssertEqualFinderVisitor(final Optional<ICPPASTCompositeTypeSpecifier> mockObject) {
    this.mockObject = mockObject;
    registrationVector = findRegistrationVectorInTestDouble();
    expectedActualPairs = list();
  }

  private Optional<IASTName> findRegistrationVectorInTestDouble() {
    return OptHelper.returnIfPresentElseEmpty(mockObject, (mockObj) -> {
      final AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
      mockObj.accept(finder);
      return finder.getFoundCallsVector();
    });
  }

  public Collection<ExpectedActualPair> getExpectedActual() {
    return expectedActualPairs;
  }

  @Override
  public int visit(final IASTStatement stmt) {
    if (stmt instanceof IASTCompoundStatement || !involvesMacroExpansion(stmt)) {
      return PROCESS_CONTINUE;
    }

    for (final IASTMacroExpansionLocation loc : getMacroExpansionLocations(stmt.getNodeLocations())) {
      final Optional<AssertKind> optKind = getAssertionKind(loc);
      if (optKind.isPresent()) {
        final Optional<ExpectedActualPair> expectedActual = optKind.get().getExpectedActual(stmt);
        if (expectedActual.isPresent()) {
          if (belongsToRegistrationVector(expectedActual.get())) {
            expectedActualPairs.add(expectedActual.get());
            return PROCESS_SKIP;
          }
        }
      }
    }
    return PROCESS_CONTINUE;
  }

  private boolean belongsToRegistrationVector(final ExpectedActualPair expectedActual) {
    return OptHelper.returnIfPresentElse(registrationVector, (vector) -> {
      final IBinding expectedActual1 = expectedActual.expected().getName().resolveBinding();
      final IBinding expectedActual2 = expectedActual.actual().getName().resolveBinding();
      return vector.resolveBinding().equals(expectedActual1) || vector.resolveBinding().equals(expectedActual2);
    }, () -> !mockObject.isPresent());
  }

  private static boolean involvesMacroExpansion(final IASTStatement stmt) {
    return stmt.getNodeLocations().length > 1;
  }

  private static Collection<IASTMacroExpansionLocation> getMacroExpansionLocations(final IASTNodeLocation[] locations) {
    final List<IASTMacroExpansionLocation> macroExpansions = list();

    for (final IASTNodeLocation loc : locations) {
      if (loc instanceof IASTMacroExpansionLocation) {
        macroExpansions.add((IASTMacroExpansionLocation) loc);
      }
    }

    return macroExpansions;
  }

  private static Optional<AssertKind> getAssertionKind(final IASTMacroExpansionLocation expansionLoc) {
    return AssertKind.fromCode(getExpansion(expansionLoc));
  }

  private static String getExpansion(final IASTMacroExpansionLocation expansionLoc) {
    return expansionLoc.getExpansion().getMacroDefinition().getName().toString();
  }
}
