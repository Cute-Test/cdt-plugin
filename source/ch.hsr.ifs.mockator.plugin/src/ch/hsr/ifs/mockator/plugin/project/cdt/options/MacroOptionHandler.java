package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;


public class MacroOptionHandler extends AbstractOptionsHandler {

   public MacroOptionHandler(IProject project) {
      super(project);
   }

   public void removeMacro(String macroName) {
      toggleMacroWrapFunName(new MacroRemover(), macroName);
   }

   public void addMacro(String macroName) {
      toggleMacroWrapFunName(new MacroAdder(), macroName);
   }

   private void toggleMacroWrapFunName(final F2<String, Collection<String>, Void> macroOp, final String macroName) {
      withEveryTool(new F2<ITool, IConfiguration, Void>() {

         @Override
         public Void apply(ITool tool, IConfiguration config) {
            IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getPreprocessorDefinesId());
            Collection<String> definedMacros = getListValues(flagsOption);
            macroOp.apply(macroName, definedMacros);
            setAndSaveOption(config, tool, flagsOption, definedMacros);
            return null;
         }
      });
   }

   @Override
   protected boolean isRequestedTool(ITool tool) {
      return isCppCompiler(tool);
   }

   private static class MacroRemover implements F2<String, Collection<String>, Void> {

      @Override
      public Void apply(String wrapFunName, Collection<String> flags) {
         flags.remove(wrapFunName);
         return null;
      }
   }

   private static class MacroAdder implements F2<String, Collection<String>, Void> {

      @Override
      public Void apply(String wrapFunName, Collection<String> flags) {
         flags.add(wrapFunName);
         return null;
      }
   }

   private static Collection<String> getListValues(IOption option) {
      try {
         return orderPreservingSet(option.getDefinedSymbols());
      }
      catch (BuildException e) {
         throw new MockatorException(e);
      }
   }
}
