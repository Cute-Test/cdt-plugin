package ch.hsr.ifs.cute.mockator.mockobject.function;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.linker.LinkerFunctionPreconVerifier;
import ch.hsr.ifs.cute.mockator.linker.WeakDeclAdder;
import ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard.MockFunctionCommunication;
import ch.hsr.ifs.cute.mockator.project.nature.NatureHandler;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;


public class MockFunctionRefactoring extends MockatorRefactoring implements MockFunctionCommunication {

    private final CppStandard cppStd;
    private final ICProject   mockatorProj;
    private IPath             destination;
    private IFile             newFile;
    private String            suiteName;

    public MockFunctionRefactoring(final CppStandard cppStd, final ICElement cElement, final Optional<ITextSelection> selection,
                                   final ICProject referencedProj, final ICProject mockatorProj) {
        super(cElement, selection);
        this.cppStd = cppStd;
        this.mockatorProj = mockatorProj;
        destination = mockatorProj.getPath();
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {
        final RefactoringStatus status = super.checkInitialConditions(pm);
        final IASTTranslationUnit ast = getAST(tu, pm);
        assureFunHasLinkSeamProperties(status, getSelectedName(ast), ast);
        return status;
    }

    private static void assureFunHasLinkSeamProperties(final RefactoringStatus status, final Optional<IASTName> selectedFunName,
            final IASTTranslationUnit ast) {
        final LinkerFunctionPreconVerifier verifier = new LinkerFunctionPreconVerifier(status, ast);
        verifier.assureSatisfiesLinkSeamProperties(selectedFunName);
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        final Optional<IASTName> funName = getSelectedName(getAST(tu, pm));
        if (funName.isPresent()) {
            final MockFunctionFileCreator fileCreator = getFileCreator(collector, pm);
            createHeaderFile(funName.get(), fileCreator);
            createSourceFile(funName.get(), fileCreator);
            setWeakDeclPropertyIfNecessary(funName.get(), collector, pm);
        }
    }

    private void createSourceFile(final IASTName selectedFunName, final MockFunctionFileCreator fileCreator) throws CoreException {
        final String suiteName = getSuiteName(selectedFunName);
        newFile = fileCreator.createSourceFile(suiteName, destination, selectedFunName);
    }

    private void createHeaderFile(final IASTName selectedFunName, final MockFunctionFileCreator fileCreator) throws CoreException {
        fileCreator.createHeaderFile(getSuiteName(selectedFunName), destination, selectedFunName);
    }

    private void setWeakDeclPropertyIfNecessary(final IASTName funName, final ModificationCollector collector, final IProgressMonitor pm) {
        new NodeLookup(getProject(), pm).findFunctionDeclaration(funName, refactoringContext).ifPresent((funDecl) -> new WeakDeclAdder(collector)
                .addWeakDeclAttribute(funDecl));
    }

    private String getSuiteName(final IASTName selectedFunName) {
        if (suiteName == null) {
            return selectedFunName.toString();
        }

        return suiteName;
    }

    private MockFunctionFileCreator getFileCreator(final ModificationCollector c, final IProgressMonitor pm) {
        if (hasMockatorProjectCuteNature()) {
            return new WithCuteSuiteFileCreator(c, refactoringContext, tu, mockatorProj, getProject(), cppStd, pm);
        } else {
            return new WithoutCuteFileCreator(c, refactoringContext, tu, mockatorProj, getProject(), cppStd, pm);
        }
    }

    private boolean hasMockatorProjectCuteNature() {
        return new NatureHandler(mockatorProj.getProject()).hasNature(MockatorConstants.CUTE_NATURE);
    }

    @Override
    public IFile getNewFile() {
        return newFile;
    }

    @Override
    public String getDescription() {
        return I18N.MockFunctionRefactoringDesc;
    }

    @Override
    public void setSuiteName(final String suiteName) {
        this.suiteName = suiteName;
    }

    @Override
    public void setDestinationFolder(final IPath destinationPath) {
        destination = destinationPath;
    }

    @Override
    public void execute(final IProgressMonitor pm) {
        final MockatorRefactoringRunner executor = new MockatorRefactoringRunner(this);
        executor.runInCurrentThread(pm);
    }
}
