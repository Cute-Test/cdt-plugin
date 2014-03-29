package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorIndexAstChecker;

public abstract class AbstractMissingMemFunChecker extends MockatorIndexAstChecker {

  protected void markIfHasMissingMemFuns(ICPPASTCompositeTypeSpecifier klass) {
    if (!hasReferencingTestFunctions(klass))
      return;

    MissingMemFunFinder finder = getMissingMemFunsFinder();

    for (MissingMemFunCodanArguments optCodanArgs : createCodanArgs(klass,
        finder.findMissingMemberFunctions(klass))) {
      mark(klass, optCodanArgs);
    }
  }

  private Maybe<MissingMemFunCodanArguments> createCodanArgs(ICPPASTCompositeTypeSpecifier klass,
      Collection<? extends MissingMemberFunction> missingMemFuns) {
    return new MissingMemFunCodanArgsProvider(getCppStandard(), missingMemFuns, klass)
        .createMemFunCodanArgs();
  }

  private boolean hasReferencingTestFunctions(ICPPASTCompositeTypeSpecifier klass) {
    ReferencingTestFunFinder finder = new ReferencingTestFunFinder(getCProject(), klass);
    Collection<ICPPASTFunctionDefinition> referencingTestFunctions = finder.findInAst(getAst());
    return !referencingTestFunctions.isEmpty();
  }

  private void mark(ICPPASTCompositeTypeSpecifier klass, MissingMemFunCodanArguments ca) {
    for (IASTName optName : getNameToMark(klass)) {
      reportProblem(getProblemId(), optName, ca.toArray());
    }
  }

  protected abstract Maybe<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier klass);

  protected abstract String getProblemId();

  protected abstract MissingMemFunFinder getMissingMemFunsFinder();

  private CppStandard getCppStandard() {
    return CppStandard.fromProjectSettings(getProject());
  }
}
