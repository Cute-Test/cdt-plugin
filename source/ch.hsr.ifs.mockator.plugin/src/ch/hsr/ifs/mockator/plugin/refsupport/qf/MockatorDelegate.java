package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.FileEditorOpener;


@SuppressWarnings("restriction")
public abstract class MockatorDelegate implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

   protected IWorkbenchWindow window;
   protected ICProject        cProject;
   protected ITextSelection   selection;
   protected ICElement        cElement;

   @Override
   public void init(final IWorkbenchWindow window) {
      this.window = window;
   }

   @Override
   public void run(final IAction action) {
      if (!(isCEditorActive() && arePreconditionsSatisfied())) {
         return;
      }

      try {
         execute();
      } catch (final RuntimeException e) {
         // pass null as message; e.getMessage() would lead to a message
         // containing twice the same text for msg and reason
         ExceptionUtil.showException(I18N.ExceptionErrorTitle, null, e);
      }
   }

   protected boolean arePreconditionsSatisfied() {
      return true;
   }

   @Override
   public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
      if (targetEditor != null) {
         window = targetEditor.getSite().getWorkbenchWindow();
      }
   }

   private boolean isCEditorActive() {
      final IWorkbenchPart activePart = window.getActivePage().getActivePart();

      if (!(activePart instanceof CEditor)) {
         return false;
      }

      final CEditor activeEditor = (CEditor) activePart;
      final IWorkingCopy wc = getWorkingCopy(activeEditor.getEditorInput());

      if (wc == null || !(wc.getResource() instanceof IFile)) {
         return false;
      }

      cProject = wc.getCProject();
      cElement = activeEditor.getInputCElement();

      if (cProject == null || cElement == null) {
         return false;
      }

      return true;
   }

   protected abstract void execute();

   private static IWorkingCopy getWorkingCopy(final IEditorInput editor) {
      final IWorkingCopyManager wcManager = CUIPlugin.getDefault().getWorkingCopyManager();
      return wcManager.getWorkingCopy(editor);
   }

   protected void openInEditor(final IFile file) {
      final FileEditorOpener opener = new FileEditorOpener(file);
      opener.openInEditor();
   }

   @Override
   public void selectionChanged(final IAction action, final ISelection newSelection) {
      if (newSelection instanceof ITextSelection) {
         selection = (ITextSelection) newSelection;
         action.setEnabled(true);
      } else {
         action.setEnabled(false);
      }
   }

   protected CppStandard getCppStd() {
      return CppStandard.fromCompilerFlags(cProject.getProject());
   }

   @Override
   public void dispose() {}
}
