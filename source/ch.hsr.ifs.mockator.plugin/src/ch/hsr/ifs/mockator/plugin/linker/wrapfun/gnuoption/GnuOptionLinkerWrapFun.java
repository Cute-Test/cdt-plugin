package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.common.DialogWithDecisionMemory;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.common.LinkerWrapFun;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf.LinkerTargetProjectFinder;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf.WrappedLinkerFlagNameCreator;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerOptionHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.MacroOptionHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;

public class GnuOptionLinkerWrapFun implements LinkerWrapFun {
  private static final String MISSING_GNU_LINUX_LINKER_KEY = "missingGnuLinuxLinkerInfo";
  private final ICProject cProject;
  private final ITextSelection selection;
  private final ICElement cElement;

  public GnuOptionLinkerWrapFun(ICProject cProject, ITextSelection selection, ICElement cElement) {
    this.cProject = cProject;
    this.selection = selection;
    this.cElement = cElement;
  }

  @Override
  public boolean arePreconditionsSatisfied() {
    return checkForGnuLinkerOnLinux();
  }

  private boolean checkForGnuLinkerOnLinux() {
    for (ToolChain optTc : ToolChain.fromProject(cProject.getProject())) {
      if (optTc == ToolChain.GnuLinux && hasGnuLinker())
        return true;
    }

    return informUser();
  }

  private boolean hasGnuLinker() {
    return ToolChain.hasLinkerForToolChain(cProject.getProject(), ToolChain.GnuLinux);
  }

  private boolean informUser() {
    String msg = I18N.WrapFunctionNoGnuLinkerFoundMsg;
    String title = I18N.WrapFunctionNoGnuLinkerFoundTitile;
    DialogWithDecisionMemory dialog =
        new DialogWithDecisionMemory(cProject.getProject(), MISSING_GNU_LINUX_LINKER_KEY);
    return dialog.informUser(title, msg);
  }

  @Override
  public void performWork() {
    createWrappingFunction();
  }

  private void createWrappingFunction() {
    final GnuOptionRefactoring refactoring = getRefactoring();
    new MockatorRefactoringRunner(refactoring).runInNewJob(new F1V<ChangeEdit>() {
      @Override
      public void apply(ChangeEdit edit) {
        addLinkerWrapperOption(refactoring.getNewFunName());
        addMacroOption(refactoring.getNewFunName());
      }
    });
  }

  private Collection<IProject> getLinkerTargetProjects() {
    return new LinkerTargetProjectFinder(cProject.getProject()).findLinkerTargetProjects();
  }

  private void addLinkerWrapperOption(String funName) {
    for (IProject proj : getLinkerTargetProjects()) {
      LinkerOptionHandler handler = new LinkerOptionHandler(proj);
      String flagName = new WrappedLinkerFlagNameCreator(funName).getWrappedLinkerFlagName();
      handler.addLinkerFlag(flagName);
    }
  }

  private void addMacroOption(String funName) {
    MacroOptionHandler handler = new MacroOptionHandler(cProject.getProject());
    handler.addMacro(MockatorConstants.WRAP_MACRO_PREFIX + funName);
  }

  private GnuOptionRefactoring getRefactoring() {
    return new GnuOptionRefactoring(cElement, selection, cProject);
  }
}
