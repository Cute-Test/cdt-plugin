package ch.hsr.ifs.cute.mockator.linker.shadowfun;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.base.util.UiUtil;
import ch.hsr.ifs.cute.mockator.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.cute.mockator.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.cute.mockator.refsupport.includes.IncludePathsProjectCopier;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;


public class ShadowFunctionDelegate extends MockatorDelegate {

    @Override
    protected void execute() {
        createShadowFunctionTu();
        addIncludesPathsToRefProjects();
    }

    @Override
    protected boolean arePreconditionsSatisfied() {
        return assureIsLibraryProject() && assureHasReferencingExecutable();
    }

    private boolean assureIsLibraryProject() {
        if (isExecutableProject()) {
            UiUtil.showInfoMessage(I18N.ShadowFunLibProjectNecessaryTitle, I18N.ShadowFunLibProjectNecessaryMsg);
            return false;
        }
        return true;
    }

    private boolean isExecutableProject() {
        return CdtManagedProjectType.fromProject(cProject.getProject()) == CdtManagedProjectType.Executable;
    }

    private boolean assureHasReferencingExecutable() {
        if (getReferencingExecutables().isEmpty()) {
            UiUtil.showInfoMessage(I18N.ShadowFunLibProjectNoRefExecTitle, I18N.ShadowFunLibProjectNoRefExecMsg);
            return false;
        }
        return true;
    }

    private Collection<IProject> getReferencingExecutables() {
        final ReferencingExecutableFinder projectFinder = new ReferencingExecutableFinder(cProject.getProject());
        return projectFinder.findReferencingExecutables();
    }

    private void createShadowFunctionTu() {
        final ShadowFunctionRefactoring refactoring = getRefactoring();
        new MockatorRefactoringRunner(refactoring).runInNewJob((ignored) -> openInEditor(refactoring.getNewFile()));
    }

    private ShadowFunctionRefactoring getRefactoring() {
        return new ShadowFunctionRefactoring(cElement, selection, cProject);
    }

    private void addIncludesPathsToRefProjects() {
        final IncludePathsProjectCopier copier = new IncludePathsProjectCopier(cProject);

        for (final IProject proj : getReferencingExecutables()) {
            copier.addIncludePaths(proj);
        }
    }
}
