package ch.hsr.ifs.cute.mockator.incompleteclass.checker;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.SimpleChecker;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.VisitorReport;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.infos.MissingMemFunInfo;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.finder.ReferencingTestFunFinder;


public abstract class AbstractMissingMemFunChecker extends SimpleChecker<ProblemId> {

    protected void markIfHasMissingMemFuns(final VisitorReport<ProblemId> result) {
        final ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) result.getNode();
        if (!hasReferencingTestFunctions(clazz)) return;
        final MissingMemFunFinder finder = getMissingMemFunsFinder();
        createInfo(clazz, finder.findMissingMemberFunctions(clazz)).ifPresent((codanArgs) -> mark(clazz, codanArgs));
    }

    private Optional<MissingMemFunInfo> createInfo(final ICPPASTCompositeTypeSpecifier clazz,
            final Collection<? extends MissingMemberFunction> missingMemFuns) {
        return new MissingMemFunInfoProvider(getCppStandard(), missingMemFuns, clazz).createInfo();
    }

    private boolean hasReferencingTestFunctions(final ICPPASTCompositeTypeSpecifier clazz) {
        final ReferencingTestFunFinder finder = new ReferencingTestFunFinder(getCProject(), clazz);
        final Collection<ICPPASTFunctionDefinition> referencingTestFunctions = finder.findInAst(getAst());
        return !referencingTestFunctions.isEmpty();
    }

    private void mark(final ICPPASTCompositeTypeSpecifier clazz, final MissingMemFunInfo info) {
        getNameToMark(clazz).ifPresent((name) -> addNodeForReporting(new VisitorReport<>(getProblemId(), clazz), info));
    }

    protected abstract Optional<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier clazz);

    protected abstract ProblemId getProblemId();

    protected abstract MissingMemFunFinder getMissingMemFunsFinder();

    private CppStandard getCppStandard() {
        return CppStandard.fromProjectSettings(getProject());
    }
}
