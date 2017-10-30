package ch.hsr.ifs.mockator.plugin.linker.wrapfun.common;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;


public class DialogWithDecisionMemory {

   private static final int OK_RETURN = 2;
   private final IProject   project;
   private final String     keyStore;

   public DialogWithDecisionMemory(IProject project, String keyToStore) {
      this.project = project;
      this.keyStore = keyToStore;
   }

   public boolean informUser(String title, String msg) {
      ScopedPreferenceStore store = getPreferenceStore(project);

      if (!alreadyAskedUser(store) || !userRejectedToSeeAgain(store)) {
         boolean ok = showDialog(title, msg, store);
         rememberDecision(store);
         return ok;
      }
      return true;
   }

   private static void rememberDecision(ScopedPreferenceStore store) {
      try {
         store.save();
      }
      catch (IOException e) {
         throw new MockatorException(e);
      }
   }

   private boolean alreadyAskedUser(IPreferenceStore store) {
      return store.contains(keyStore);
   }

   private boolean userRejectedToSeeAgain(IPreferenceStore store) {
      return store.getString(keyStore).equals(MessageDialogWithToggle.ALWAYS);
   }

   private boolean showDialog(String title, String msg, IPreferenceStore store) {
      boolean toggleState = false;
      MessageDialogWithToggle dialog = MessageDialogWithToggle.open(MessageDialog.QUESTION, UiUtil.getWindowShell(), title, msg,
            I18N.WrapFunctionDoNotShowAgainMsg, toggleState, store, keyStore, SWT.SHEET);
      return dialog.getReturnCode() == OK_RETURN;
   }

   private static ScopedPreferenceStore getPreferenceStore(IProject project) {
      ProjectScope ps = new ProjectScope(project);
      ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, MockatorPlugin.PLUGIN_ID);
      return scoped;
   }
}
