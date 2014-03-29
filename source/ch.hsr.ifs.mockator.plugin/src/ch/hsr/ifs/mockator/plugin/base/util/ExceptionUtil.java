package ch.hsr.ifs.mockator.plugin.base.util;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.last;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public abstract class ExceptionUtil {

  public static Maybe<Throwable> getRootCause(Throwable t) {
    List<Throwable> causes = getCausesOf(t);
    return causes.size() < 2 ? Maybe.<Throwable>none() : last(causes);
  }

  private static List<Throwable> getCausesOf(Throwable t) {
    List<Throwable> causes = list();

    while (t != null && !causes.contains(t)) {
      causes.add(t);
      t = t.getCause();
    }
    return causes;
  }

  public static void showException(Exception exToShow) {
    UiUtil.runInDisplayThread(new F1V<Exception>() {
      @Override
      public void apply(Exception e) {
        showExceptionInThread(e);
      }
    }, exToShow);
  }

  private static void showExceptionInThread(Exception e) {
    showExceptionInThread(I18N.ExceptionCaughtTitle, I18N.ExceptionCaughtMessage, e);
  }

  public static void showException(final String title, final String message, final Throwable t) {
    UiUtil.runInDisplayThread(new F1V<Void>() {
      @Override
      public void apply(Void notUsed) {
        showExceptionInThread(title, message, t);
      }
    }, null);
  }

  private static void showExceptionInThread(String title, String message, Throwable t) {
    IStatus status =
        new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID, IStatus.OK,
            (t.getMessage() == null) ? t.getClass().getName() : t.getMessage(), t);
    MockatorPlugin.getDefault().getLog().log(status);
    ErrorDialog.openError(UiUtil.getWindowShell(), title, message, status);
  }
}
