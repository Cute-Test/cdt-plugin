package ch.hsr.ifs.mockator.plugin.base.util;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

@SuppressWarnings("restriction")
public abstract class UiUtil {

  public static <T> void runInDisplayThread(final F1V<T> callBack, final T param) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        callBack.apply(param);
      }
    };
    Display display = getDisplay();

    if (isDisplayThreadCurrentThread(display)) {
      runnable.run();
    } else {
      display.syncExec(runnable);
    }
  }

  private static Display getDisplay() {
    Display display = PlatformUI.getWorkbench().getDisplay();
    Assert.isFalse(display == null || display.isDisposed(),
        "Display should not be null or already disposed");
    return display;
  }

  private static boolean isDisplayThreadCurrentThread(Display display) {
    return Thread.currentThread().equals(display.getThread());
  }

  public static Maybe<CEditor> getActiveCEditor() {
    IEditorPart editor = getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (editor instanceof CEditor)
      return maybe((CEditor) editor);
    return none();
  }

  public static IWorkbenchWindow getActiveWorkbenchWindow() {
    IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    Assert.notNull(activeWindow, "Not called from the UI thread");
    return activeWindow;
  }

  public static Shell getWindowShell() {
    return getActiveWorkbenchWindow().getShell();
  }

  public static Maybe<IDocument> getCurrentDocument() {
    for (CEditor optEditor : getActiveCEditor()) {
      IEditorInput editorInput = optEditor.getEditorInput();
      return maybe(optEditor.getDocumentProvider().getDocument(editorInput));
    }
    return none();
  }

  public static void showInfoMessage(String title, String msg) {
    MessageDialog.open(MessageDialog.INFORMATION, getWindowShell(), title, msg, SWT.NONE);
  }
}
