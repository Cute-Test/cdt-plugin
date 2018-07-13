package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


public class FileEditorOpener {

   private final IFile file;

   public FileEditorOpener(final IFile file) {
      this.file = file;
   }

   public void openInEditor() {
      final IWorkbenchPage activePage = getActivePage();

      if (activePage == null) return;

      try {
         final String editorId = getEditorId();
         // activate should be false otherwise this triggers another switch
         // to a different editor
         final boolean activate = false;
         activePage.openEditor(new FileEditorInput(file), editorId, activate);
      } catch (final Exception e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private static IWorkbenchPage getActivePage() {
      return CUIPlugin.getActivePage();
   }

   private String getEditorId() throws CoreException {
      final IContentType contentType = getFileContentType();
      IEditorDescriptor desc = getEditorDescriptor(contentType);

      if (desc == null) {
         desc = getDefaultEditor();
      }

      return desc.getId();
   }

   private IEditorDescriptor getEditorDescriptor(final IContentType contentType) {
      return getEditorRegistry().getDefaultEditor(file.getName(), contentType);
   }

   private static IEditorRegistry getEditorRegistry() {
      return PlatformUI.getWorkbench().getEditorRegistry();
   }

   private static IEditorDescriptor getDefaultEditor() {
      return getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
   }

   private IContentType getFileContentType() throws CoreException {
      final IContentDescription desc = file.getContentDescription();

      if (desc != null) return desc.getContentType();

      return null;
   }
}
