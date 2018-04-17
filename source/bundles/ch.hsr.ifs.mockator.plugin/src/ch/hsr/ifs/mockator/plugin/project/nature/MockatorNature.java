package ch.hsr.ifs.mockator.plugin.project.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerLibraryHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


public class MockatorNature implements IProjectNature {

   public static final String  NATURE_ID       = "ch.hsr.ifs.mockator.MockatorNature";
   private static final String BOOST_REGEX_LIB = "boost_regex";
   private IProject            project;

   public static void addMockatorNature(final IProject project, final IProgressMonitor pm) throws CoreException {
      final NatureHandler handler = new NatureHandler(project);
      handler.addNature(NATURE_ID, pm);
   }

   public static void removeMockatorNature(final IProject project, final IProgressMonitor pm) throws CoreException {
      final NatureHandler handler = new NatureHandler(project);
      handler.removeNature(NATURE_ID, pm);
   }

   @Override
   public void configure() throws CoreException {
      new MockatorLibHandler(project).addLibToProject();
      getCppStandard().toggleCppStdSupport(project);
      //TODO(Tobias Stauber) remove boost dep
      addBoostRegexLibrary(project);
   }

   private static void addBoostRegexLibrary(final IProject project) {
      new LinkerLibraryHandler(project).addLibrary(BOOST_REGEX_LIB);
   }

   @Override
   public void deconfigure() throws CoreException {
      new MockatorLibHandler(project).removeLibFromProject();
      //TODO(Tobias Stauber) remove boost dep
      removeBoostRegexLibrary(project);
   }

   private static void removeBoostRegexLibrary(final IProject project) {
      new LinkerLibraryHandler(project).removeLibrary(BOOST_REGEX_LIB);
   }

   @Override
   public IProject getProject() {
      return project;
   }

   @Override
   public void setProject(final IProject project) {
      this.project = project;
   }

   private CppStandard getCppStandard() {
      return CppStandard.fromProjectSettings(project);
   }
}
