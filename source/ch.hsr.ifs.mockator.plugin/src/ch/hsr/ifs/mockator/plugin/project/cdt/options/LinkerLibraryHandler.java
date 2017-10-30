package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;


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

   private void toggleLibrary(final F2<String, Set<String>, Void> libraryOp, final String libName) {
      withEveryTool(new F2<ITool, IConfiguration, Void>() {

         @Override
         public Void apply(final ITool tool, final IConfiguration config) {
            for (final IOption option : tool.getOptions()) {
               try {
                  if (option.getValueType() == IOption.LIBRARIES) {
                     final Set<String> libs = orderPreservingSet(option.getLibraries());
                     libraryOp.apply(libName, libs);
                     setAndSaveOption(config, tool, option, libs);
                  }
               } catch (final BuildException e) {
                  throw new MockatorException(e);
               }
            }

            return null;
         }
      });
   }

   private static class LibraryAdder implements F2<String, Set<String>, Void> {

      @Override
      public Void apply(final String newLibrary, final Set<String> libraries) {
         libraries.add(newLibrary);
         return null;
      }
   }

   private static class LibraryRemover implements F2<String, Set<String>, Void> {

      @Override
      public Void apply(final String newLibrary, final Set<String> libraries) {
         libraries.remove(newLibrary);
         return null;
      }
   }

   public boolean hasLibrary(final String libName) {
      return OptHelper.returnIfPresentElse(getToolToAnanalyze(), (tool) -> {
         for (final IOption option : tool.getOptions()) {
            try {
               if (option.getValueType() == IOption.LIBRARIES) {
                  final Set<String> libs = orderPreservingSet(option.getLibraries());
                  return libs.contains(libName);
               }
            } catch (final BuildException e) {
               throw new MockatorException(e);
            }
         }
         return false;
      }, () -> false);
   }

   @Override
   protected boolean isRequestedTool(final ITool tool) {
      return isLinker(tool);
   }
}
