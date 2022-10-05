package ch.hsr.ifs.cute.mockator.base.util;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


@SuppressWarnings("restriction")
public abstract class UiUtil {

    public static <T> void runInDisplayThread(final Consumer<T> callBack, final T param) {
        final Runnable runnable = () -> callBack.accept(param);
        final Display display = getDisplay();

        if (isDisplayThreadCurrentThread(display)) {
            runnable.run();
        } else {
            display.syncExec(runnable);
        }
    }

    private static Display getDisplay() {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        ILTISException.Unless.isFalse("Display should not be null or already disposed", display == null || display.isDisposed());
        return display;
    }

    private static boolean isDisplayThreadCurrentThread(final Display display) {
        return Thread.currentThread().equals(display.getThread());
    }

    public static Optional<CEditor> getActiveCEditor() {
        final IEditorPart editor = getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof ICEditor) {
            return Optional.of((CEditor) editor);
        }
        return Optional.empty();
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ILTISException.Unless.notNull("Not called from the UI thread", activeWindow);
        return activeWindow;
    }

    public static Shell getWindowShell() {
        return getActiveWorkbenchWindow().getShell();
    }

    public static Optional<IDocument> getCurrentDocument() {
        // TODO: quick hack to get rid of compile error
        return Optional.empty();// getActiveCEditor().map((editor) -> editor.getDocumentProvider().getDocument(editor.getEditorInput()));
    }

    public static void showInfoMessage(final String title, final String msg) {
        MessageDialog.open(MessageDialog.INFORMATION, getWindowShell(), title, msg, SWT.NONE);
    }
}
