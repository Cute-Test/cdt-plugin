package ch.hsr.ifs.cute.mockator.project.cdt;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


// Caution: CDT also adds exported settings like include paths and libraries from
// the to be referenced project to the referencing project; if we only need to
// have a project reference, we better set the reference via the IProjectDescription
public class ReferencingProjectAdder {

   private final IProject originProject;

   public ReferencingProjectAdder(final IProject originProject) {
      this.originProject = originProject;
   }

   public void setReferenceToProject(final IProject referencedProject) throws CoreException {
      final ICProjectDescription desc = getProjectDescription();

      for (final ICConfigurationDescription config : desc.getConfigurations()) {
         final Map<String, String> refMap = config.getReferenceInfo();
         refMap.put(referencedProject.getName(), "");
         config.setReferenceInfo(refMap);
      }

      storeProjectDescription(desc);
   }

   private void storeProjectDescription(final ICProjectDescription desc) throws CoreException {
      CCorePlugin.getDefault().setProjectDescription(originProject, desc);
   }

   private ICProjectDescription getProjectDescription() {
      final boolean writableDesc = true;
      return CCorePlugin.getDefault().getProjectDescription(originProject, writableDesc);
   }
}
