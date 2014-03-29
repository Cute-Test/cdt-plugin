package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
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

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
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
  private static final String MISSING_GNU_LINUX_OR_MACOSX_KEY = "missingGnuLinuxOrMacOSXInfo";
  private final ICProject cProject;
  private final ITextSelection selection;
  private final ICElement cElement;
  private final CppStandard cppStd;
  private String newProjectName;

  public LdPreloadLinkerWrapFun(ICProject proj, ITextSelection sel, ICElement el, CppStandard std) {
    this.cProject = proj;
    this.selection = sel;
    this.cElement = el;
    this.cppStd = std;
  }

  @Override
  public boolean arePreconditionsSatisfied() {
    return assureHasReferencingExecutable() && assureIsMacOrLinuxGnuToolchain();
  }

  private boolean assureIsMacOrLinuxGnuToolchain() {
    for (ToolChain optTc : ToolChain.fromProject(cProject.getProject())) {
      switch (optTc) {
        case GnuLinux:
        case GnuMacOSX:
          return true;
        default:
          break;
      }
    }
    return informUser();
  }

  private boolean informUser() {
    IProject project = cProject.getProject();
    DialogWithDecisionMemory dialog =
        new DialogWithDecisionMemory(project, MISSING_GNU_LINUX_OR_MACOSX_KEY);
    return dialog.informUser(I18N.RuntimeWrapLibWindowsNotSupportedTitle,
        I18N.RuntimeWrapLibWindowsNotSupportedMsg);
  }

  private boolean assureHasReferencingExecutable() {
    Collection<IProject> referencingExecutables = getReferencingExecutables();

    if (referencingExecutables.isEmpty()) {
      UiUtil.showInfoMessage(I18N.RuntimeWrapLibProjectNoRefExecTitle,
          I18N.RuntimeWrapLibProjectNoRefExecMsg);
      return false;
    }
    return true;
  }

  @Override
  public void performWork() {
    Job runtimeJob = getRuntimePreloadInfrastructureJob();
    runtimeJob.schedule();
  }

  private Job getRuntimePreloadInfrastructureJob() {
    Job runtimeJob = new Job(I18N.RuntimeWrapCreateInfrastructure) {
      @Override
      protected IStatus run(IProgressMonitor pm) {
        try {
          initNewProjectName();
          createAndConfigureShLibProject(pm);
          addIncludePathsToNewProject();
          performRefactoring(pm);

          for (IProject proj : getReferencingExecutables()) {
            setProjectReference(proj, pm);
            addRuntimeConfiguration(proj);
          }
        } catch (CoreException e) {
          return new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID,
              I18N.RuntimeWrapCreateInfrastructureFailed, e);
        }
        return Status.OK_STATUS;
      }
    };
    runtimeJob.setUser(true);
    return runtimeJob;
  }

  private Collection<IProject> getReferencingExecutables() {
    ReferencingExecutableFinder projectFinder =
        new ReferencingExecutableFinder(cProject.getProject());
    return projectFinder.findReferencingExecutables();
  }

  private void addRuntimeConfiguration(IProject referencingExecutable) {
    IProject sharedLibProj = getNewlyCreatedProject();
    RunConfigEnvManager manager = new RunConfigEnvManager(referencingExecutable, sharedLibProj);
    LibraryPathResolver resolver = new LibraryPathResolver(sharedLibProj);
    manager.addPreloadLaunchConfig(resolver.getLibraryWorkspacePath());
  }

  private void performRefactoring(IProgressMonitor monitor) {
    LdPreloadRefactoring refactoring = getRefactoring(cElement, getNewlyCreatedProject());
    new MockatorRefactoringRunner(refactoring).runInCurrentThread(monitor);
    openInEditor(refactoring.getNewFile());
  }

  private static void openInEditor(IFile file) {
    UiUtil.runInDisplayThread(new F1V<IFile>() {
      @Override
      public void apply(IFile fileToOpen) {
        FileEditorOpener opener = new FileEditorOpener(fileToOpen);
        opener.openInEditor();
      }
    }, file);
  }

  protected LdPreloadRefactoring getRefactoring(ICElement cElement, IProject targetProject) {
    return new LdPreloadRefactoring(cppStd, cElement, selection, cProject, targetProject);
  }

  private void addIncludePathsToNewProject() {
    IncludePathsProjectCopier copier = new IncludePathsProjectCopier(cProject);
    copier.addIncludePaths(getNewlyCreatedProject());
  }

  private void setProjectReference(IProject referencingExecutable, IProgressMonitor pm)
      throws CoreException {
    IProjectDescription execDesc = referencingExecutable.getDescription();
    Set<IProject> existingProjRefs = orderPreservingSet(execDesc.getReferencedProjects());
    existingProjRefs.add(getNewlyCreatedProject());
    execDesc.setReferencedProjects(existingProjRefs.toArray(new IProject[existingProjRefs.size()]));
    referencingExecutable.setDescription(execDesc, pm);
  }

  private IProject getNewlyCreatedProject() {
    return ProjectUtil.getWorkspaceRoot().getProject(newProjectName);
  }

  private void createAndConfigureShLibProject(IProgressMonitor monitor) throws CoreException {
    IProject project = createSharedLibProject(monitor);
    addRuntimeDynamicLibSupport(project);
    setCpp0xSupportIfNecessary(project);
    activatePositionIndependentCode(project);
  }

  private IProject createSharedLibProject(IProgressMonitor monitor) throws CoreException {
    SharedLibProjectCreator sharedLibCreator =
        new SharedLibProjectCreator(newProjectName, cProject.getProject());
    return sharedLibCreator.createSharedLib(monitor);
  }

  private void setCpp0xSupportIfNecessary(IProject project) {
    if (cppStd == CppStandard.Cpp11Std) {
      cppStd.toggleCppStdSupport(project);
    }
  }

  private static void addRuntimeDynamicLibSupport(IProject project) {
    addLibrary(project, MockatorConstants.DYNAMIC_LIB_NAME);
  }

  private void initNewProjectName() {
    newProjectName =
        new UniqueProjectNameCreator(selection.getText() + "Lib").getUniqueProjectName();
  }

  private static void addLibrary(IProject project, String libName) {
    new LinkerLibraryHandler(project).addLibrary(libName);
  }

  private static void activatePositionIndependentCode(IProject project) {
    new CompilerPicOptionHandler(project).setPositionIndependentCode();
  }
}
