package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorFinderVisitor;

public class AssertEqualFinderVisitor extends ASTVisitor {
  private final Maybe<ICPPASTCompositeTypeSpecifier> mockObject;
  private final Maybe<IASTName> registrationVector;
  private final List<ExpectedActualPair> expectedActualPairs;

  {
    shouldVisitStatements = true;
  }

  public AssertEqualFinderVisitor(Maybe<ICPPASTCompositeTypeSpecifier> mockObject) {
    this.mockObject = mockObject;
    registrationVector = findRegistrationVectorInTestDouble();
    expectedActualPairs = list();
  }

  private Maybe<IASTName> findRegistrationVectorInTestDouble() {
    for (ICPPASTCompositeTypeSpecifier optMockObject : mockObject) {
      AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
      optMockObject.accept(finder);
      return finder.getFoundCallsVector();
    }
    return none();
  }

  public Collection<ExpectedActualPair> getExpectedActual() {
    return expectedActualPairs;
  }

  @Override
  public int visit(IASTStatement stmt) {
    if (stmt instanceof IASTCompoundStatement || !involvesMacroExpansion(stmt))
      return PROCESS_CONTINUE;

    for (IASTMacroExpansionLocation loc : getMacroExpansionLocations(stmt.getNodeLocations())) {
      for (AssertKind optKind : getAssertionKind(loc)) {
        for (ExpectedActualPair optExpectedActual : optKind.getExpectedActual(stmt)) {
          if (belongsToRegistrationVector(optExpectedActual)) {
            expectedActualPairs.add(optExpectedActual);
            return PROCESS_SKIP;
          }
        }
      }
    }
    return PROCESS_CONTINUE;
  }

  private boolean belongsToRegistrationVector(ExpectedActualPair expectedActual) {
    for (IASTName optVector : registrationVector) {
      IBinding expectedActual1 = _1(expectedActual).getName().resolveBinding();
      IBinding expectedActual2 = _2(expectedActual).getName().resolveBinding();
      return optVector.resolveBinding().equals(expectedActual1)
          || optVector.resolveBinding().equals(expectedActual2);
    }

    return mockObject.isNone();
  }

  private static boolean involvesMacroExpansion(IASTStatement stmt) {
    return stmt.getNodeLocations().length > 1;
  }

  private static Collection<IASTMacroExpansionLocation> getMacroExpansionLocations(
      IASTNodeLocation[] locations) {
    List<IASTMacroExpansionLocation> macroExpansions = list();

    for (IASTNodeLocation loc : locations) {
      if (loc instanceof IASTMacroExpansionLocation) {
        macroExpansions.add((IASTMacroExpansionLocation) loc);
      }
    }

    return macroExpansions;
  }

  private static Maybe<AssertKind> getAssertionKind(IASTMacroExpansionLocation expansionLoc) {
    return AssertKind.fromCode(getExpansion(expansionLoc));
  }

  private static String getExpansion(IASTMacroExpansionLocation expansionLoc) {
    return expansionLoc.getExpansion().getMacroDefinition().getName().toString();
  }
}
