package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.common.DialogWithDecisionMemory;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.common.LinkerWrapFun;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf.LinkerTargetProjectFinder;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf.WrappedLinkerFlagNameCreator;
import ch.hsr.ifs.cute.mockator.project.cdt.options.LinkerOptionHandler;
import ch.hsr.ifs.cute.mockator.project.cdt.options.MacroOptionHandler;
import ch.hsr.ifs.cute.mockator.project.cdt.toolchains.ToolChain;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;


public class GnuOptionLinkerWrapFun implements LinkerWrapFun {

   private static final String            MISSING_GNU_LINUX_LINKER_KEY = "missingGnuLinuxLinkerInfo";
   private final ICProject                cProject;
   private final Optional<ITextSelection> selection;
   private final ICElement                cElement;

   public GnuOptionLinkerWrapFun(final ICProject cProject, final Optional<ITextSelection> selection, final ICElement cElement) {
      this.cProject = cProject;
      this.selection = selection;
      this.cElement = cElement;
   }

   @Override
   public boolean arePreconditionsSatisfied() {
      return checkForGnuLinkerOnLinux();
   }

   private boolean checkForGnuLinkerOnLinux() {
      return ToolChain.fromProject(cProject.getProject()).map(optTc -> optTc == ToolChain.GnuLinux && hasGnuLinker() ? true : informUser()).orElse(
            informUser());
   }

   private boolean hasGnuLinker() {
      return ToolChain.hasLinkerForToolChain(cProject.getProject(), ToolChain.GnuLinux);
   }

   private boolean informUser() {
      final String msg = I18N.WrapFunctionNoGnuLinkerFoundMsg;
      final String title = I18N.WrapFunctionNoGnuLinkerFoundTitile;
      final DialogWithDecisionMemory dialog = new DialogWithDecisionMemory(cProject.getProject(), MISSING_GNU_LINUX_LINKER_KEY);
      return dialog.informUser(title, msg);
   }

   @Override
   public void performWork() {
      createWrappingFunction();
   }

   private void createWrappingFunction() {
      final GnuOptionRefactoring refactoring = getRefactoring();
      new MockatorRefactoringRunner(refactoring).runInNewJob((edit) -> {
         addLinkerWrapperOption(refactoring.getNewFunName());
         addMacroOption(refactoring.getNewFunName());
      });
   }

   private Collection<IProject> getLinkerTargetProjects() {
      return new LinkerTargetProjectFinder(cProject.getProject()).findLinkerTargetProjects();
   }

   private void addLinkerWrapperOption(final String funName) {
      for (final IProject proj : getLinkerTargetProjects()) {
         final LinkerOptionHandler handler = new LinkerOptionHandler(proj);
         final String flagName = new WrappedLinkerFlagNameCreator(funName).getWrappedLinkerFlagName();
         handler.addLinkerFlag(flagName);
      }
   }

   private void addMacroOption(final String funName) {
      final MacroOptionHandler handler = new MacroOptionHandler(cProject.getProject());
      handler.addMacro(MockatorConstants.WRAP_MACRO_PREFIX + funName);
   }

   private GnuOptionRefactoring getRefactoring() {
      return new GnuOptionRefactoring(cElement, selection, cProject);
   }
}
