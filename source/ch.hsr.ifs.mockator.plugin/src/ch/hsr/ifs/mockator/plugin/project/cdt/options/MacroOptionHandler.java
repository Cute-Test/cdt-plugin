package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.functions.Function2;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public class MacroOptionHandler extends AbstractOptionsHandler {

   public MacroOptionHandler(final IProject project) {
      super(project);
   }

   public void removeMacro(final String macroName) {
      toggleMacroWrapFunName(new MacroRemover(), macroName);
   }

   public void addMacro(final String macroName) {
      toggleMacroWrapFunName(new MacroAdder(), macroName);
   }

   private void toggleMacroWrapFunName(final Function2<String, Collection<String>, Void> macroOp, final String macroName) {
      withEveryTool((tool, config) -> {
         final IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getPreprocessorDefinesId());
         final Collection<String> definedMacros = getListValues(flagsOption);
         macroOp.apply(macroName, definedMacros);
         setAndSaveOption(config, tool, flagsOption, definedMacros);
         return null;
      });
   }

   @Override
   protected boolean isRequestedTool(final ITool tool) {
      return isCppCompiler(tool);
   }

   private static class MacroRemover implements Function2<String, Collection<String>, Void> {

      @Override
      public Void apply(final String wrapFunName, final Collection<String> flags) {
         flags.remove(wrapFunName);
         return null;
      }
   }

   private static class MacroAdder implements Function2<String, Collection<String>, Void> {

      @Override
      public Void apply(final String wrapFunName, final Collection<String> flags) {
         flags.add(wrapFunName);
         return null;
      }
   }

   private static Collection<String> getListValues(final IOption option) {
      try {
         return orderPreservingSet(option.getDefinedSymbols());
      }
      catch (final BuildException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
