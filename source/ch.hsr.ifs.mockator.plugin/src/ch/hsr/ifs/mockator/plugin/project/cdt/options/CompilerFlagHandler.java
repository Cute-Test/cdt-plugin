package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;


public class CompilerFlagHandler extends AbstractOptionsHandler {

   public CompilerFlagHandler(final IProject project) {
      super(project);
   }

   public void addCompilerFlag(final String flag) {
      toggleCompilerFlag(new CompilerFlagAdder(), flag);
   }

   public void removeCompilerFlag(final String flag) {
      toggleCompilerFlag(new CompilerFlagRemover(), flag);
   }

   public String getCompilerFlags() {
      return OptHelper.returnIfPresentElseNull(getToolToAnanalyze(), (tool) -> {
         try {
            return tool.getToolCommandFlagsString(null, null);
         }
         catch (final BuildException e) {
            return null;
         }
      });
   }

   private void toggleCompilerFlag(final F2<String, String, String> compilerFlagOp, final String flag) {
      withEveryTool(new F2<ITool, IConfiguration, Void>() {

         @Override
         public Void apply(final ITool tool, final IConfiguration config) {
            try {
               final IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getCppCompilerOtherFlagsId());
               final String flags = flagsOption.getStringValue();
               final String newFlags = compilerFlagOp.apply(flag, flags);
               setAndSaveOption(config, tool, flagsOption, newFlags);
            }
            catch (final BuildException e) {
               throw new MockatorException(e);
            }

            return null;
         }
      });
   }

   private static class CompilerFlagRemover implements F2<String, String, String> {

      @Override
      public String apply(final String flagToRemove, final String flags) {
         return flags.replaceAll(Pattern.quote(flagToRemove), "");
      }
   }

   private static class CompilerFlagAdder implements F2<String, String, String> {

      @Override
      public String apply(final String flagToAdd, final String flags) {
         if (!flags.contains(flagToAdd)) {
            return flags + " " + flagToAdd;
         }
         return flags;
      }
   }

   @Override
   protected boolean isRequestedTool(final ITool tool) {
      return isCppCompiler(tool);
   }
}
