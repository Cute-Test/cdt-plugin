package ch.hsr.ifs.mockator.plugin.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;
import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard.NewSuiteFileCreationWizard;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludePathsProjectCopier;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;


public class MockFunctionDelegate extends MockatorDelegate implements IWorkbenchWindowActionDelegate {

   @Override
   protected boolean arePreconditionsSatisfied() {
      return assureIsLibraryProject();
   }

   @Override
   protected void execute() {
      getReferencingMockatorProject().ifPresent((proj) -> {
         if (isCuteProject(proj)) {
            showCuteSuiteWizard(proj);
         } else {
            mockFreeFunction(proj);
         }
         addProjectIncludesTo(proj);
      });
   }

   private void addProjectIncludesTo(final IProject refProj) {
      final IncludePathsProjectCopier copier = new IncludePathsProjectCopier(cProject);
      copier.addIncludePaths(refProj);
   }

   private void showCuteSuiteWizard(final IProject mockatorProject) {
      final ICProject cProject = CProjectUtil.getCProject(mockatorProject);
      final MockFunctionRefactoring refactoring = getMockFunRefactoring(cProject);
      final LinkSuiteToRunnerRefactoring runner = getRunnerRefactoring(cProject);
      createAndOpenWizard(new NewSuiteFileCreationWizard(cProject, refactoring, runner));
   }

   private static void createAndOpenWizard(final NewSuiteFileCreationWizard wizard) {
      final WizardDialog dialog = new WizardDialog(UiUtil.getWindowShell(), wizard);
      dialog.create();
      dialog.open();
   }

   private MockFunctionRefactoring getMockFunRefactoring(final ICProject mockatorCProject) {
      final CppStandard cppStandard = getCppStandard(mockatorCProject.getProject());
      return new MockFunctionRefactoring(cppStandard, cElement, selection, cProject, mockatorCProject);
   }

   private LinkSuiteToRunnerRefactoring getRunnerRefactoring(final ICProject mockatorCProject) {
      return new LinkSuiteToRunnerRefactoring(cElement, selection, mockatorCProject);
   }

   private static boolean isCuteProject(final IProject project) {
      return new NatureHandler(project).hasNature(MockatorConstants.CUTE_NATURE);
   }

   private boolean assureIsLibraryProject() {
      if (CdtManagedProjectType.fromProject(cProject.getProject()) == CdtManagedProjectType.Executable) {
         UiUtil.showInfoMessage(I18N.MockFunctionPreconditionsNotSatisfied, I18N.MockFunctionOnlyWorksWithLibs);
         return false;
      }
      return true;
   }

   private Optional<IProject> getReferencingMockatorProject() {
      final Collection<IProject> referencingExecutables = getReferencingMockatorExecutables();

      if (referencingExecutables.isEmpty()) {
         UiUtil.showInfoMessage(I18N.MockFunctionPreconditionsNotSatisfied, I18N.MockFunctionNeedsMockatorProject);
         return Optional.empty();
      }
      return head(referencingExecutables);
   }

   private Collection<IProject> getReferencingMockatorExecutables() {
      final ReferencingExecutableFinder projectFinder = new ReferencingExecutableFinder(cProject.getProject());
      return projectFinder.findReferencingMockatorExecutables();
   }

   private void mockFreeFunction(final IProject mockatorProject) {
      final MockFunctionRefactoring refactoring = getMockFunRefactoring(CProjectUtil.getCProject(mockatorProject));
      new MockatorRefactoringRunner(refactoring).runInNewJob((ignored) -> openInEditor(refactoring.getNewFile()));
   }

   private static CppStandard getCppStandard(final IProject project) {
      return CppStandard.fromCompilerFlags(project);
   }
}
