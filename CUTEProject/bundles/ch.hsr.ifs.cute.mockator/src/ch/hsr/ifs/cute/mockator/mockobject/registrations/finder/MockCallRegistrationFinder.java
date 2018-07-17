package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.head;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.CallRegistrationFinder;


public class MockCallRegistrationFinder implements CallRegistrationFinder {

   private final CppStandard cppStd;

   public MockCallRegistrationFinder(final CppStandard cppStd) {
      this.cppStd = cppStd;
   }

   @Override
   public Optional<ExistingMemFunCallRegistration> findRegisteredCall(final ICPPASTFunctionDefinition function) {
      final List<IASTName> usages = getRegistrationVectorUsesIn(function);
      return head(collectRealRegistrations(usages));
   }

   private Collection<ExistingMemFunCallRegistration> collectRealRegistrations(final List<IASTName> callRegistrations) {
      return new CallRegistrationCollector(cppStd).getRegistrations(callRegistrations);
   }

   private List<IASTName> getRegistrationVectorUsesIn(final ICPPASTFunctionDefinition function) {
      final List<IASTName> callRegistrations = new ArrayList<>();
      function.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(final IASTName name) {
            if (hasCallsVectorType(name)) {
               callRegistrations.add(name);
               return PROCESS_SKIP;
            }
            return PROCESS_CONTINUE;
         }

         private boolean hasCallsVectorType(final IASTName name) {
            final IASTNode parent = name.getParent();

            if (!(parent instanceof IASTIdExpression)) { return false; }

            return new CallsVectorTypeVerifier((IASTIdExpression) parent).isVectorOfCallsVector();
         }
      });
      return callRegistrations;
   }
}
