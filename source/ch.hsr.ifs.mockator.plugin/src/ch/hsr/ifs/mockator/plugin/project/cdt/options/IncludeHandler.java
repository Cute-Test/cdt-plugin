package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import ch.hsr.ifs.iltis.core.functional.functions.Function2;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


abstract class IncludeHandler extends AbstractOptionsHandler {

   public IncludeHandler(final IProject project) {
      super(project);
   }

   public <U extends IResource> void addInclude(final U resource) {
      alterIncludesToAllConfigs(new IncludeAdder(), resource);
   }

   public <U extends IResource> void removeInclude(final U resource) {
      alterIncludesToAllConfigs(new IncludeRemover(), resource);
   }

   public Collection<String> getAllIncludes() {
      final Optional<ITool> tool = getToolToAnanalyze();
      if (tool.isPresent()) {
         for (final IOption option : tool.get().getOptions()) {
            try {
               if (option.getValueType() == getOptionType()) { return getOptionValues(option); }
            }
            catch (final BuildException e) {}
         }
      }

      throw new ILTISException("Problems determining includes").rethrowUnchecked();
   }

   public <U extends IResource> boolean hasInclude(final U folder) {
      final String includePath = getWorkspacePath(folder);

      final Optional<ITool> tool = getToolToAnanalyze();
      if (tool.isPresent()) {
         for (final IOption option : tool.get().getOptions()) {
            try {
               if (option.getValueType() == getOptionType()) { return getOptionValues(option).contains(includePath); }
            }
            catch (final BuildException e) {}
         }
      }

      throw new ILTISException("Problems determining includes").rethrowUnchecked();
   }

   @Override
   protected boolean isRequestedTool(final ITool tool) {
      return isCppCompiler(tool);
   }

   private <U extends IResource> void alterIncludesToAllConfigs(final Function2<String, Set<String>, Void> includePathOp, final U folder) {
      final String includePath = getWorkspacePath(folder);
      withEveryTool((tool, config) -> {
         for (final IOption option : tool.getOptions()) {
            try {
               if (option.getValueType() == getOptionType()) {
                  final Set<String> includePaths = getOptionValues(option);
                  includePathOp.apply(includePath, includePaths);
                  setAndSaveOption(config, tool, option, includePaths);
               }
            }
            catch (final BuildException e) {
               throw new ILTISException(e).rethrowUnchecked();
            }
         }

         return null;
      });
   }

   protected abstract int getOptionType();

   protected abstract Set<String> getOptionValues(IOption option) throws BuildException;

   private static <U extends IResource> String getWorkspacePath(final U folder) {
      return ProjRelPathGenerator.getProjectRelativePath(folder);
   }

   private static class IncludeAdder implements Function2<String, Set<String>, Void> {

      @Override
      public Void apply(final String newIncludePath, final Set<String> includePaths) {
         includePaths.add(newIncludePath);
         return null;
      }
   }

   private static class IncludeRemover implements Function2<String, Set<String>, Void> {

      @Override
      public Void apply(final String includePathToRemove, final Set<String> includePaths) {
         includePaths.remove(includePathToRemove);
         return null;
      }
   }
}
