package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.resources.WorkspaceUtil;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.common.DialogWithDecisionMemory;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.common.LinkerWrapFun;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring.LdPreloadRefactoring;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig.RunConfigEnvManager;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.CompilerPicOptionHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerLibraryHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludePathsProjectCopier;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.FileEditorOpener;


public class LdPreloadLinkerWrapFun implements LinkerWrapFun {

   private static final String            MISSING_GNU_LINUX_OR_MACOSX_KEY = "missingGnuLinuxOrMacOSXInfo";
   private final ICProject                cProject;
   private final ICElement                cElement;
   private final CppStandard              cppStd;
   private String                         newProjectName;
   private final Optional<ITextSelection> selection;

   public LdPreloadLinkerWrapFun(final ICProject proj, final Optional<ITextSelection> sel, final ICElement el, final CppStandard std) {
      cProject = proj;
      cElement = el;
      selection = sel;
      cppStd = std;
   }

   @Override
   public boolean arePreconditionsSatisfied() {
      return assureHasReferencingExecutable() && assureIsMacOrLinuxGnuToolchain();
   }

   private boolean assureIsMacOrLinuxGnuToolchain() {
      return ToolChain.fromProject(cProject.getProject()).map((tc) -> {
         switch (tc) {
         case GnuLinux:
         case GnuMacOSX:
            return true;
         default:
            return informUser();
         }
      }).orElse(informUser());
   }

   private boolean informUser() {
      final IProject project = cProject.getProject();
      final DialogWithDecisionMemory dialog = new DialogWithDecisionMemory(project, MISSING_GNU_LINUX_OR_MACOSX_KEY);
      return dialog.informUser(I18N.RuntimeWrapLibWindowsNotSupportedTitle, I18N.RuntimeWrapLibWindowsNotSupportedMsg);
   }

   private boolean assureHasReferencingExecutable() {
      final Collection<IProject> referencingExecutables = getReferencingExecutables();

      if (referencingExecutables.isEmpty()) {
         UiUtil.showInfoMessage(I18N.RuntimeWrapLibProjectNoRefExecTitle, I18N.RuntimeWrapLibProjectNoRefExecMsg);
         return false;
      }
      return true;
   }

   @Override
   public void performWork() {
      final Job runtimeJob = getRuntimePreloadInfrastructureJob();
      runtimeJob.schedule();
   }

   private Job getRuntimePreloadInfrastructureJob() {
      final Job runtimeJob = new Job(I18N.RuntimeWrapCreateInfrastructure) {

         @Override
         protected IStatus run(final IProgressMonitor pm) {
            try {
               initNewProjectName();
               createAndConfigureShLibProject(pm);
               addIncludePathsToNewProject();
               performRefactoring(pm);

               for (final IProject proj : getReferencingExecutables()) {
                  setProjectReference(proj, pm);
                  addRuntimeConfiguration(proj);
               }
            } catch (final CoreException e) {
               return new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID, I18N.RuntimeWrapCreateInfrastructureFailed, e);
            }
            return Status.OK_STATUS;
         }
      };
      runtimeJob.setUser(true);
      return runtimeJob;
   }

   private Collection<IProject> getReferencingExecutables() {
      final ReferencingExecutableFinder projectFinder = new ReferencingExecutableFinder(cProject.getProject());
      return projectFinder.findReferencingExecutables();
   }

   private void addRuntimeConfiguration(final IProject referencingExecutable) {
      final IProject sharedLibProj = getNewlyCreatedProject();
      final RunConfigEnvManager manager = new RunConfigEnvManager(referencingExecutable, sharedLibProj);
      final LibraryPathResolver resolver = new LibraryPathResolver(sharedLibProj);
      manager.addPreloadLaunchConfig(resolver.getLibraryWorkspacePath());
   }

   private void performRefactoring(final IProgressMonitor monitor) {
      final LdPreloadRefactoring refactoring = getRefactoring(cElement, getNewlyCreatedProject());
      new MockatorRefactoringRunner(refactoring).runInCurrentThread(monitor);
      openInEditor(refactoring.getNewFile());
   }

   private static void openInEditor(final IFile file) {
      UiUtil.runInDisplayThread((fileToOpen) -> {
         final FileEditorOpener opener = new FileEditorOpener(fileToOpen);
         opener.openInEditor();
      }, file);
   }

   protected LdPreloadRefactoring getRefactoring(final ICElement cElement, final IProject targetProject) {
      return new LdPreloadRefactoring(cppStd, cElement, selection, cProject, targetProject);
   }

   private void addIncludePathsToNewProject() {
      final IncludePathsProjectCopier copier = new IncludePathsProjectCopier(cProject);
      copier.addIncludePaths(getNewlyCreatedProject());
   }

   private void setProjectReference(final IProject referencingExecutable, final IProgressMonitor pm) throws CoreException {
      final IProjectDescription execDesc = referencingExecutable.getDescription();
      final Set<IProject> existingProjRefs = orderPreservingSet(execDesc.getReferencedProjects());
      existingProjRefs.add(getNewlyCreatedProject());
      execDesc.setReferencedProjects(existingProjRefs.toArray(new IProject[existingProjRefs.size()]));
      referencingExecutable.setDescription(execDesc, pm);
   }

   private IProject getNewlyCreatedProject() {
      return WorkspaceUtil.getWorkspaceRoot().getProject(newProjectName);
   }

   private void createAndConfigureShLibProject(final IProgressMonitor monitor) throws CoreException {
      final IProject project = createSharedLibProject(monitor);
      addRuntimeDynamicLibSupport(project);
      setCpp0xSupportIfNecessary(project);
      activatePositionIndependentCode(project);
   }

   private IProject createSharedLibProject(final IProgressMonitor monitor) throws CoreException {
      final SharedLibProjectCreator sharedLibCreator = new SharedLibProjectCreator(newProjectName, cProject.getProject());
      return sharedLibCreator.createSharedLib(monitor);
   }

   private void setCpp0xSupportIfNecessary(final IProject project) {
      if (cppStd == CppStandard.Cpp11Std) {
         cppStd.toggleCppStdSupport(project);
      }
   }

   private static void addRuntimeDynamicLibSupport(final IProject project) {
      addLibrary(project, MockatorConstants.DYNAMIC_LIB_NAME);
   }

   private void initNewProjectName() {
      newProjectName = new UniqueProjectNameCreator(selection.map(ITextSelection::getText).orElse("") + "Lib").getUniqueProjectName();
   }

   private static void addLibrary(final IProject project, final String libName) {
      new LinkerLibraryHandler(project).addLibrary(libName);
   }

   private static void activatePositionIndependentCode(final IProject project) {
      new CompilerPicOptionHandler(project).setPositionIndependentCode();
   }
}
