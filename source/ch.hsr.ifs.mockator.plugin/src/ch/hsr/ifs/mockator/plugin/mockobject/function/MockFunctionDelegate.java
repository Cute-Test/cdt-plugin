package ch.hsr.ifs.mockator.plugin.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard.NewSuiteFileCreationWizard;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludePathsProjectCopier;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;

public class MockFunctionDelegate extends MockatorDelegate implements
    IWorkbenchWindowActionDelegate {

  @Override
  protected boolean arePreconditionsSatisfied() {
    return assureIsLibraryProject();
  }

  @Override
  protected void execute() {
    for (IProject optProj : getReferencingMockatorProject()) {
      if (isCuteProject(optProj)) {
        showCuteSuiteWizard(optProj);
      } else {
        mockFreeFunction(optProj);
      }
      addProjectIncludesTo(optProj);
    }
  }

  private void addProjectIncludesTo(IProject refProj) {
    IncludePathsProjectCopier copier = new IncludePathsProjectCopier(cProject);
    copier.addIncludePaths(refProj);
  }

  private void showCuteSuiteWizard(IProject mockatorProject) {
    ICProject cProject = ProjectUtil.getCProject(mockatorProject);
    MockFunctionRefactoring refactoring = getMockFunRefactoring(cProject);
    LinkSuiteToRunnerRefactoring runner = getRunnerRefactoring(cProject);
    createAndOpenWizard(new NewSuiteFileCreationWizard(cProject, refactoring, runner));
  }

  private static void createAndOpenWizard(NewSuiteFileCreationWizard wizard) {
    WizardDialog dialog = new WizardDialog(UiUtil.getWindowShell(), wizard);
    dialog.create();
    dialog.open();
  }

  private MockFunctionRefactoring getMockFunRefactoring(ICProject mockatorCProject) {
    CppStandard cppStandard = getCppStandard(mockatorCProject.getProject());
    return new MockFunctionRefactoring(cppStandard, cElement, selection, cProject, mockatorCProject);
  }

  private LinkSuiteToRunnerRefactoring getRunnerRefactoring(ICProject mockatorCProject) {
    return new LinkSuiteToRunnerRefactoring(cElement, selection, mockatorCProject);
  }

  private static boolean isCuteProject(IProject project) {
    return new NatureHandler(project).hasNature(MockatorConstants.CUTE_NATURE);
  }

  private boolean assureIsLibraryProject() {
    if (CdtManagedProjectType.fromProject(cProject.getProject()) == CdtManagedProjectType.Executable) {
      UiUtil.showInfoMessage(I18N.MockFunctionPreconditionsNotSatisfied,
          I18N.MockFunctionOnlyWorksWithLibs);
      return false;
    }
    return true;
  }

  private Maybe<IProject> getReferencingMockatorProject() {
    Collection<IProject> referencingExecutables = getReferencingMockatorExecutables();

    if (referencingExecutables.isEmpty()) {
      UiUtil.showInfoMessage(I18N.MockFunctionPreconditionsNotSatisfied,
          I18N.MockFunctionNeedsMockatorProject);
      return none();
    }
    return head(referencingExecutables);
  }

  private Collection<IProject> getReferencingMockatorExecutables() {
    ReferencingExecutableFinder projectFinder =
        new ReferencingExecutableFinder(cProject.getProject());
    return projectFinder.findReferencingMockatorExecutables();
  }

  private void mockFreeFunction(IProject mockatorProject) {
    final MockFunctionRefactoring refactoring =
        getMockFunRefactoring(ProjectUtil.getCProject(mockatorProject));
    new MockatorRefactoringRunner(refactoring).runInNewJob(new F1V<ChangeEdit>() {
      @Override
      public void apply(ChangeEdit notUsed) {
        openInEditor(refactoring.getNewFile());
      }
    });
  }

  private static CppStandard getCppStandard(IProject project) {
    return CppStandard.fromCompilerFlags(project);
  }
}
