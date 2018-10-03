package ch.hsr.ifs.cute.mockator.extractinterface;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.cute.mockator.infos.ExtractInterfaceInfo;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;


public class ExtractInterfaceRefactoring extends MockatorRefactoring {

    private final ExtractInterfaceContext context;
    private ExtractInterfaceHandler       handler;

    public ExtractInterfaceRefactoring(final ExtractInterfaceContext context) {
        super(context.getTuOfSelection(), context.getSelection());
        this.context = context;
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        final RefactoringStatus status = super.checkInitialConditions(pm);
        pm.subTask(I18N.ExtractInterfaceAnalyzingInProgress);
        prepareRefactoring(status, pm);
        handler.preProcess();
        return status;
    }

    private void prepareRefactoring(final RefactoringStatus status, final IProgressMonitor pm) throws OperationCanceledException, CoreException {
        context.setStatus(status);
        context.setSelectedName(getSelectedName(getAST(getTranslationUnit(), pm)));
        context.setProgressMonitor(pm);
        context.setCRefContext(refactoringContext);
        handler = new ExtractInterfaceHandler(context);
    }

    @Override
    protected RefactoringStatus checkFinalConditions(final IProgressMonitor pm, final CheckConditionsContext c) throws CoreException {
        final RefactoringStatus status = c.check(pm);

        try {
            handler.postProcess(status);
        } catch (final Exception e) {
            status.addFatalError("Extract interface refactoring failed: " + e.getMessage());
        }

        return status;
    }

    @Override
    protected RefactoringDescriptor getRefactoringDescriptor() {
        return new ExtractInterfaceDescriptor(project.getProject().getName(), "Extract Interface Refactoring", getRefactoringDescription(),
                new ExtractInterfaceInfo().also(i -> {
                    i.interfaceName = context.getNewInterfaceName();
                    i.replaceAllOccurences = context.shouldReplaceAllOccurences();
                }));
    }

    private String getRefactoringDescription() {
        final String className = ASTUtil.getQfNameF(context.getChosenClass());
        return String.format("Extract interface for class '%s'", className);
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector c) throws CoreException, OperationCanceledException {
        pm.subTask(I18N.ExtractInterfacePerformingChangesInProgress);
        context.setModificationCollector(c);
        context.setProgressMonitor(pm);
        handler.performChanges();
    }

    public ExtractInterfaceContext getMockatorContext() {
        return context;
    }

    @Override
    public String getDescription() {
        return I18N.ExtractInterfaceName;
    }
}
