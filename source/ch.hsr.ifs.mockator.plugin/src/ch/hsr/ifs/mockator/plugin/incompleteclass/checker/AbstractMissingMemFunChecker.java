package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.ast.checker.CheckerResult;
import ch.hsr.ifs.iltis.cpp.ast.checker.SimpleChecker;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;


public abstract class AbstractMissingMemFunChecker extends SimpleChecker<ProblemId> {

   protected void markIfHasMissingMemFuns(final CheckerResult<ProblemId> result) {
      final ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) result.getNode();
      if (!hasReferencingTestFunctions(clazz)) {
         return;
      }
      final MissingMemFunFinder finder = getMissingMemFunsFinder();
      createCodanArgs(clazz, finder.findMissingMemberFunctions(clazz)).ifPresent((codanArgs) -> mark(clazz, codanArgs));
   }

   private Optional<MissingMemFunCodanArguments> createCodanArgs(final ICPPASTCompositeTypeSpecifier clazz,
            final Collection<? extends MissingMemberFunction> missingMemFuns) {
      return new MissingMemFunCodanArgsProvider(getCppStandard(), missingMemFuns, clazz).createMemFunCodanArgs();
   }

   private boolean hasReferencingTestFunctions(final ICPPASTCompositeTypeSpecifier clazz) {
      final ReferencingTestFunFinder finder = new ReferencingTestFunFinder(getCProject(), clazz);
      final Collection<ICPPASTFunctionDefinition> referencingTestFunctions = finder.findInAst(getAst());
      return !referencingTestFunctions.isEmpty();
   }

   private void mark(final ICPPASTCompositeTypeSpecifier clazz, final MissingMemFunCodanArguments ca) {
      getNameToMark(clazz).ifPresent((name) -> addNodeForReporting(new CheckerResult<>(getProblemId(), clazz), ca.toArray()));
   }

   protected abstract Optional<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier clazz);

   protected abstract ProblemId getProblemId();

   protected abstract MissingMemFunFinder getMissingMemFunsFinder();

   private CppStandard getCppStandard() {
      return CppStandard.fromProjectSettings(getProject());
   }
}
