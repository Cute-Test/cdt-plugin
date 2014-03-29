package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.CallRegistrationFinder;

public class MockCallRegistrationFinder implements CallRegistrationFinder {
  private final CppStandard cppStd;

  public MockCallRegistrationFinder(CppStandard cppStd) {
    this.cppStd = cppStd;
  }

  @Override
  public Maybe<ExistingMemFunCallRegistration> findRegisteredCall(ICPPASTFunctionDefinition function) {
    List<IASTName> usages = getRegistrationVectorUsesIn(function);
    return head(collectRealRegistrations(usages));
  }

  private Collection<ExistingMemFunCallRegistration> collectRealRegistrations(
      List<IASTName> callRegistrations) {
    return new CallRegistrationCollector(cppStd).getRegistrations(callRegistrations);
  }

  private List<IASTName> getRegistrationVectorUsesIn(ICPPASTFunctionDefinition function) {
    final List<IASTName> callRegistrations = list();
    function.accept(new ASTVisitor() {
      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
        if (hasCallsVectorType(name)) {
          callRegistrations.add(name);
          return PROCESS_SKIP;
        }
        return PROCESS_CONTINUE;
      }

      private boolean hasCallsVectorType(IASTName name) {
        IASTNode parent = name.getParent();

        if (!(parent instanceof IASTIdExpression))
          return false;

        return new CallsVectorTypeVerifier((IASTIdExpression) parent).isVectorOfCallsVector();
      }
    });
    return callRegistrations;
  }
}
