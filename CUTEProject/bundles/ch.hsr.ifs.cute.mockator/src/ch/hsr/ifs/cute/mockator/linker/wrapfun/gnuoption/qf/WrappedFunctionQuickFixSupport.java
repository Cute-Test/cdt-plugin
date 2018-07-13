package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.project.cdt.options.LinkerOptionHandler;
import ch.hsr.ifs.cute.mockator.project.cdt.options.MacroOptionHandler;


class WrappedFunctionQuickFixSupport {

   private final IProject project;

   WrappedFunctionQuickFixSupport(final IProject project) {
      this.project = project;
   }

   boolean isWrappedFunctionActive(final String wrapFunName) {
      for (final IProject proj : getLinkerTargetProjects()) {
         final LinkerOptionHandler flagHandler = new LinkerOptionHandler(proj);

         if (flagHandler.hasLinkerFlag(getWrappedLinkerFlagName(wrapFunName))) return true;
      }

      return false;
   }

   void addWrapLinkerOption(final String wrapFunName) {
      withLinkerTargetProjects((project) -> {
         final LinkerOptionHandler flagHandler = new LinkerOptionHandler(project);
         flagHandler.addLinkerFlag(getWrappedLinkerFlagName(wrapFunName));
      });
   }

   private static String getWrappedLinkerFlagName(final String wrapFunName) {
      return new WrappedLinkerFlagNameCreator(wrapFunName).getWrappedLinkerFlagName();
   }

   void addWrapMacro(final String wrapFunName) {
      final MacroOptionHandler macroHandler = new MacroOptionHandler(project);
      macroHandler.addMacro(getWrappedFunMacroName(wrapFunName));
   }

   void removeWrapLinkerOption(final String wrapFunName) {
      withLinkerTargetProjects((project) -> {
         final LinkerOptionHandler flagHandler = new LinkerOptionHandler(project);
         flagHandler.removeLinkerFlag(getWrappedLinkerFlagName(wrapFunName));
      });
   }

   void removeWrapMacro(final String wrapFunName) {
      final MacroOptionHandler macroHandler = new MacroOptionHandler(project);
      macroHandler.removeMacro(getWrappedFunMacroName(wrapFunName));
   }

   private static String getWrappedFunMacroName(final String wrapFunName) {
      return MockatorConstants.WRAP_MACRO_PREFIX + wrapFunName;
   }

   private void withLinkerTargetProjects(final Consumer<IProject> fun) {
      for (final IProject proj : getLinkerTargetProjects()) {
         fun.accept(proj);
      }
   }

   private Collection<IProject> getLinkerTargetProjects() {
      return new LinkerTargetProjectFinder(project).findLinkerTargetProjects();
   }
}
