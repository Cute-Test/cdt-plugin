package ch.hsr.ifs.cute.mockator.testdouble.movetons;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.core.functional.OptionalUtil;

import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;


public class MoveTestDoubleToNsRefactoring extends MockatorRefactoring {

    private final CppStandard         cppStd;
    private ICPPASTFunctionDefinition testFunction;

    public MoveTestDoubleToNsRefactoring(final CppStandard cppStd, final ICElement cElement, final Optional<ITextSelection> selection,
                                         final ICProject cProject) {
        super(cElement, selection);
        this.cppStd = cppStd;
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        final RefactoringStatus status = super.checkInitialConditions(pm);

        if (!findFirstEnclosingClass(selection).isPresent()) {
            status.addFatalError(NO_CLASS_FOUND_IN_SELECTION);
            return status;
        }

        checkSelectedNameIsInFunction(status, pm);
        return status;
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        OptionalUtil.of(findFirstEnclosingClass(selection)).ifPresentT(cls -> {
            final ASTRewrite rewriter = collector.rewriterForTranslationUnit(getAST(tu, pm));
            testFunction = getParentFunction(cls.getName());
            moveToNamespace(cls, rewriter);
        });
    }

    private void moveToNamespace(final ICPPASTCompositeTypeSpecifier optClass, final ASTRewrite rewriter) {
        new TestDoubleToNsMover(rewriter, cppStd).moveToNamespace(optClass);
    }

    public ICPPASTFunctionDefinition getTestFunction() {
        return testFunction;
    }

    @Override
    public String getDescription() {
        return I18N.MoveTestDoubleToNsRefactoringDesc;
    }
}
