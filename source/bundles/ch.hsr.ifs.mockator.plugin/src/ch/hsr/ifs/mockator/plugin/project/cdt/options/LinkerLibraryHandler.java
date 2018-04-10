package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.functions.Function2;


public class LinkerLibraryHandler extends AbstractOptionsHandler {

   public LinkerLibraryHandler(final IProject project) {
      super(project);
   }

   public void addLibrary(final String libName) {
      toggleLibrary(new LibraryAdder(), libName);
   }

   public void removeLibrary(final String libName) {
      toggleLibrary(new LibraryRemover(), libName);
   }

   private void toggleLibrary(final Function2<String, Set<String>, Void> libraryOp, final String libName) {
      withEveryTool((tool, config) -> {
         for (final IOption option : tool.getOptions()) {
            try {
               if (option.getValueType() == IOption.LIBRARIES) {
                  final Set<String> libs = orderPreservingSet(option.getLibraries());
                  libraryOp.apply(libName, libs);
                  setAndSaveOption(config, tool, option, libs);
               }
            } catch (final BuildException e) {
               throw new ILTISException(e).rethrowUnchecked();
            }
         }

         return null;
      });
   }

   private static class LibraryAdder implements Function2<String, Set<String>, Void> {

      @Override
      public Void apply(final String newLibrary, final Set<String> libraries) {
         libraries.add(newLibrary);
         return null;
      }
   }

   private static class LibraryRemover implements Function2<String, Set<String>, Void> {

      @Override
      public Void apply(final String newLibrary, final Set<String> libraries) {
         libraries.remove(newLibrary);
         return null;
      }
   }

   public boolean hasLibrary(final String libName) {
      return getToolToAnanalyze().map(tool -> {
         for (final IOption option : tool.getOptions()) {
            try {
               if (option.getValueType() == IOption.LIBRARIES) {
                  final Set<String> libs = orderPreservingSet(option.getLibraries());
                  return libs.contains(libName);
               }
            } catch (final BuildException e) {
               throw new ILTISException(e).rethrowUnchecked();
            }
         }
         return false;
      }).orElse(false);
   }

   @Override
   protected boolean isRequestedTool(final ITool tool) {
      return isLinker(tool);
   }
}
