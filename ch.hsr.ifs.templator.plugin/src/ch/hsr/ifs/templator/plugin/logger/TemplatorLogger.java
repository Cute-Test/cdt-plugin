package ch.hsr.ifs.templator.plugin.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import ch.hsr.ifs.templator.plugin.TemplatorPlugin;

public final class TemplatorLogger {

	private TemplatorLogger() {
	}

	public static void logInfo(String message) {
		log(IStatus.INFO, IStatus.OK, message, null);
	}

	public static void log(int severity, int code, String message, Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}

	public static void logError(Throwable exception) {
		logError(Objects.toString(exception.getMessage(), "Unexpected exception"), exception);
	}

	public static void logError(String message, Throwable exception) {
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}

	public static IStatus createStatus(int severity, int code, String message, Throwable exception) {
		return new Status(severity, TemplatorPlugin.PLUGIN_ID, code, message, exception);
	}

	public static void log(IStatus status) {
		TemplatorPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Shows JFace ErrorDialog but improved by constructing full stack trace in detail area.
	 */
	public static void errorDialogWithStackTrace(String title, String msg, Throwable t) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);

		final String trace = sw.toString(); // stack trace as a string

		List<Status> childStatuses = new ArrayList<>();

		for (String line : trace.split(System.getProperty("line.separator"))) {
			childStatuses.add(new Status(IStatus.ERROR, TemplatorPlugin.PLUGIN_ID, line));
		}

		MultiStatus ms = new MultiStatus(TemplatorPlugin.PLUGIN_ID, IStatus.ERROR,
				childStatuses.toArray(new Status[] {}), t.getLocalizedMessage(), t);

		ErrorDialog.openError(null, title, msg, ms);
	}
}