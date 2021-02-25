package ch.hsr.ifs.cute.mockator.linker.wrapfun.common;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.MockatorPlugin;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.base.util.UiUtil;


public class DialogWithDecisionMemory {

    private static final int OK_RETURN = 2;
    private final IProject   project;
    private final String     keyStore;

    public DialogWithDecisionMemory(final IProject project, final String keyToStore) {
        this.project = project;
        keyStore = keyToStore;
    }

    public boolean informUser(final String title, final String msg) {
        final ScopedPreferenceStore store = getPreferenceStore(project);

        if (!alreadyAskedUser(store) || !userRejectedToSeeAgain(store)) {
            final boolean ok = showDialog(title, msg, store);
            rememberDecision(store);
            return ok;
        }
        return true;
    }

    private static void rememberDecision(final ScopedPreferenceStore store) {
        try {
            store.save();
        } catch (final IOException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private boolean alreadyAskedUser(final IPreferenceStore store) {
        return store.contains(keyStore);
    }

    private boolean userRejectedToSeeAgain(final IPreferenceStore store) {
        return store.getString(keyStore).equals(MessageDialogWithToggle.ALWAYS);
    }

    private boolean showDialog(final String title, final String msg, final IPreferenceStore store) {
        final boolean toggleState = false;
        final MessageDialogWithToggle dialog = MessageDialogWithToggle.open(MessageDialog.QUESTION, UiUtil.getWindowShell(), title, msg,
                I18N.WrapFunctionDoNotShowAgainMsg, toggleState, store, keyStore, SWT.SHEET);
        return dialog.getReturnCode() == OK_RETURN;
    }

    private static ScopedPreferenceStore getPreferenceStore(final IProject project) {
        final ProjectScope ps = new ProjectScope(project);
        final ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, MockatorPlugin.PLUGIN_ID);
        return scoped;
    }
}
