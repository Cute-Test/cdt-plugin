package ch.hsr.ifs.mockator.plugin.mockobject.function;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.linker.LinkerFunctionPreconVerifier;
import ch.hsr.ifs.mockator.plugin.linker.WeakDeclAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard.MockFunctionCommunication;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;

@SuppressWarnings("restriction")
public class MockFunctionRefactoring extends MockatorRefactoring implements
    MockFunctionCommunication {
  private final CppStandard cppStd;
  private final ICProject mockatorProj;
  private IPath destination;
  private IFile newFile;
  private String suiteName;

  public MockFunctionRefactoring(CppStandard cppStd, ICElement cElement, ITextSelection selection,
      ICProject referencedProj, ICProject mockatorProj) {
    super(cElement, selection, referencedProj);
    this.cppStd = cppStd;
    this.mockatorProj = mockatorProj;
    destination = mockatorProj.getPath();
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    IASTTranslationUnit ast = getAST(tu, pm);
    assureFunHasLinkSeamProperties(status, getSelectedName(ast), ast);
    return status;
  }

  private static void assureFunHasLinkSeamProperties(RefactoringStatus status,
      Maybe<IASTName> selectedFunName, IASTTranslationUnit ast) {
    LinkerFunctionPreconVerifier verifier = new LinkerFunctionPreconVerifier(status, ast);
    verifier.assureSatisfiesLinkSeamProperties(selectedFunName);
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    for (IASTName optFunName : getSelectedName(getAST(tu, pm))) {
      MockFunctionFileCreator fileCreator = getFileCreator(collector, pm);
      createHeaderFile(optFunName, fileCreator);
      createSourceFile(optFunName, fileCreator);
      setWeakDeclPropertyIfNecessary(optFunName, collector, pm);
    }
  }

  private void createSourceFile(IASTName selectedFunName, MockFunctionFileCreator fileCreator)
      throws CoreException {
    String suiteName = getSuiteName(selectedFunName);
    newFile = fileCreator.createSourceFile(suiteName, destination, selectedFunName);
  }

  private void createHeaderFile(IASTName selectedFunName, MockFunctionFileCreator fileCreator)
      throws CoreException {
    fileCreator.createHeaderFile(getSuiteName(selectedFunName), destination, selectedFunName);
  }

  private void setWeakDeclPropertyIfNecessary(IASTName funName, ModificationCollector collector,
      IProgressMonitor pm) {
    NodeLookup lookup = new NodeLookup(project, pm);
    for (ICPPASTFunctionDeclarator optFunDecl : lookup.findFunctionDeclaration(funName,
        refactoringContext)) {
      WeakDeclAdder weakDeclAdder = new WeakDeclAdder(collector);
      weakDeclAdder.addWeakDeclAttribute(optFunDecl);
    }
  }

  private String getSuiteName(IASTName selectedFunName) {
    if (suiteName == null)
      return selectedFunName.toString();

    return suiteName;
  }

  private MockFunctionFileCreator getFileCreator(ModificationCollector c, IProgressMonitor pm) {
    if (hasMockatorProjectCuteNature())
      return new WithCuteSuiteFileCreator(c, refactoringContext, tu, mockatorProj, project, cppStd,
          pm);
    else
      return new WithoutCuteFileCreator(c, refactoringContext, tu, mockatorProj, project, cppStd,
          pm);
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
  public void setSuiteName(String suiteName) {
    this.suiteName = suiteName;
  }

  @Override
  public void setDestinationFolder(IPath destinationPath) {
    this.destination = destinationPath;
  }

  @Override
  public void execute(IProgressMonitor pm) {
    MockatorRefactoringRunner executor = new MockatorRefactoringRunner(this);
    executor.runInCurrentThread(pm);
  }
}
