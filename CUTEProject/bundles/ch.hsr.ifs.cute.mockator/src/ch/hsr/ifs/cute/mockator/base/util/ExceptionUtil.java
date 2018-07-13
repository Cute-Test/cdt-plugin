package ch.hsr.ifs.cute.mockator.base.util;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.last;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import ch.hsr.ifs.cute.mockator.MockatorPlugin;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;


public abstract class ExceptionUtil {

   public static Optional<Throwable> getRootCause(final Throwable t) {
      final List<Throwable> causes = getCausesOf(t);
      return causes.size() < 2 ? Optional.empty() : last(causes);
   }

   private static List<Throwable> getCausesOf(Throwable t) {
      final List<Throwable> causes = new ArrayList<>();

      while (t != null && !causes.contains(t)) {
         causes.add(t);
         t = t.getCause();
      }
      return causes;
   }

   public static void showException(final Exception exToShow) {
      UiUtil.runInDisplayThread(ExceptionUtil::showExceptionInThread, exToShow);
   }

   private static void showExceptionInThread(final Exception e) {
      showExceptionInThread(I18N.ExceptionCaughtTitle, I18N.ExceptionCaughtMessage, e);
   }

   public static void showException(final String title, final String message, final Throwable t) {
      UiUtil.runInDisplayThread((ignored) -> showExceptionInThread(title, message, t), null);
   }

   private static void showExceptionInThread(final String title, final String message, final Throwable t) {
      final IStatus status = new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID, IStatus.OK, t.getMessage() == null ? t.getClass().getName() : t
            .getMessage(), t);
      MockatorPlugin.getDefault().getLog().log(status);
      ErrorDialog.openError(UiUtil.getWindowShell(), title, message, status);
   }
}
