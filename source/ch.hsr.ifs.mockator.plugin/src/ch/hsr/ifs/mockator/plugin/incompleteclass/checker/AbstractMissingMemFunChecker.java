package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorIndexAstChecker;


public abstract class AbstractMissingMemFunChecker extends MockatorIndexAstChecker {

   protected void markIfHasMissingMemFuns(final ICPPASTCompositeTypeSpecifier klass) {
      if (!hasReferencingTestFunctions(klass)) { return; }
      final MissingMemFunFinder finder = getMissingMemFunsFinder();
      createCodanArgs(klass, finder.findMissingMemberFunctions(klass)).ifPresent((codanArgs) -> mark(klass, codanArgs));
   }

   private Optional<MissingMemFunCodanArguments> createCodanArgs(final ICPPASTCompositeTypeSpecifier klass,
         final Collection<? extends MissingMemberFunction> missingMemFuns) {
      return new MissingMemFunCodanArgsProvider(getCppStandard(), missingMemFuns, klass).createMemFunCodanArgs();
   }

   private boolean hasReferencingTestFunctions(final ICPPASTCompositeTypeSpecifier klass) {
      final ReferencingTestFunFinder finder = new ReferencingTestFunFinder(getCProject(), klass);
      final Collection<ICPPASTFunctionDefinition> referencingTestFunctions = finder.findInAst(getAst());
      return !referencingTestFunctions.isEmpty();
   }

   private void mark(final ICPPASTCompositeTypeSpecifier klass, final MissingMemFunCodanArguments ca) {
      getNameToMark(klass).ifPresent((name) -> reportProblem(getProblemId(), name, ca.toArray()));
   }

   protected abstract Optional<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier klass);

   protected abstract String getProblemId();

   protected abstract MissingMemFunFinder getMissingMemFunsFinder();

   private CppStandard getCppStandard() {
      return CppStandard.fromProjectSettings(getProject());
   }
}
