package ch.hsr.ifs.mockator.plugin.project.cdt;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class SourceFolderHandler {

   private final IProject project;

   public SourceFolderHandler(final IProject project) {
      this.project = project;
   }

   public IFolder createFolder(final String relativePath, final IProgressMonitor pm) throws CoreException {
      final IFolder newFolder = project.getFolder(relativePath);

      if (!newFolder.exists()) {
         createFolder(newFolder, pm);
      }
      addToSourceEntries(newFolder, pm);

      return newFolder;
   }

   private static void createFolder(final IFolder folder, final IProgressMonitor pm) throws CoreException {
      final IContainer parent = folder.getParent();

      if (parent instanceof IFolder) {
         createFolder((IFolder) parent, pm);
      }

      folder.create(true, true, pm);
   }

   public void deleteFolder(final String relativePath, final IProgressMonitor pm) throws CoreException {
      final IFolder folder = project.getFolder(relativePath);

      if (folder.exists()) {
         folder.delete(true, pm);
      }
   }

   private void addToSourceEntries(final IFolder newFolder, final IProgressMonitor pm) throws CoreException {
      final ICSourceEntry newEntry = new CSourceEntry(newFolder, null, ICSettingEntry.SOURCE_PATH);
      final boolean writableDesc = true;
      final ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, writableDesc);
      if (desc != null) {
         addNewSourceEntry(desc, newEntry);
         CCorePlugin.getDefault().setProjectDescription(project, desc, true, pm);
      }
   }

   private static void addNewSourceEntry(final ICProjectDescription desc, final ICSourceEntry entry) throws WriteAccessException, CoreException {
      for (final ICConfigurationDescription configDesc : desc.getConfigurations()) {
         final ICSourceEntry[] entries = configDesc.getSourceEntries();
         configDesc.setSourceEntries(addEntry(entries, entry));
      }
   }

   private static ICSourceEntry[] addEntry(final ICSourceEntry[] oldEntries, final ICSourceEntry entry) {
      final Set<ICSourceEntry> newEntries = orderPreservingSet(oldEntries);
      newEntries.add(entry);
      return newEntries.toArray(new ICSourceEntry[newEntries.size()]);
   }
}
