package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerOptionHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.MacroOptionHandler;


class WrappedFunctionQuickFixSupport {

   private final IProject project;

   WrappedFunctionQuickFixSupport(IProject project) {
      this.project = project;
   }

   boolean isWrappedFunctionActive(String wrapFunName) {
      for (IProject proj : getLinkerTargetProjects()) {
         LinkerOptionHandler flagHandler = new LinkerOptionHandler(proj);

         if (flagHandler.hasLinkerFlag(getWrappedLinkerFlagName(wrapFunName))) return true;
      }

      return false;
   }

   void addWrapLinkerOption(final String wrapFunName) {
      withLinkerTargetProjects(new F1V<IProject>() {

         @Override
         public void apply(IProject project) {
            LinkerOptionHandler flagHandler = new LinkerOptionHandler(project);
            flagHandler.addLinkerFlag(getWrappedLinkerFlagName(wrapFunName));
         }
      });
   }

   private static String getWrappedLinkerFlagName(String wrapFunName) {
      return new WrappedLinkerFlagNameCreator(wrapFunName).getWrappedLinkerFlagName();
   }

   void addWrapMacro(String wrapFunName) {
      MacroOptionHandler macroHandler = new MacroOptionHandler(project);
      macroHandler.addMacro(getWrappedFunMacroName(wrapFunName));
   }

   void removeWrapLinkerOption(final String wrapFunName) {
      withLinkerTargetProjects(new F1V<IProject>() {

         @Override
         public void apply(IProject project) {
            LinkerOptionHandler flagHandler = new LinkerOptionHandler(project);
            flagHandler.removeLinkerFlag(getWrappedLinkerFlagName(wrapFunName));
         }
      });
   }

   void removeWrapMacro(String wrapFunName) {
      MacroOptionHandler macroHandler = new MacroOptionHandler(project);
      macroHandler.removeMacro(getWrappedFunMacroName(wrapFunName));
   }

   private static String getWrappedFunMacroName(String wrapFunName) {
      return MockatorConstants.WRAP_MACRO_PREFIX + wrapFunName;
   }

   private void withLinkerTargetProjects(F1V<IProject> fun) {
      for (IProject proj : getLinkerTargetProjects()) {
         fun.apply(proj);
      }
   }

   private Collection<IProject> getLinkerTargetProjects() {
      return new LinkerTargetProjectFinder(project).findLinkerTargetProjects();
   }
}
