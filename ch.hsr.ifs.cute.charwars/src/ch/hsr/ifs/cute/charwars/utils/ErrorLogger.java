package ch.hsr.ifs.cute.charwars.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

import ch.hsr.ifs.cute.charwars.CharWarsPlugin;

public final class ErrorLogger {
	private ErrorLogger() {}
	
	public static void log(String message, Throwable exception) {
		ILog logger = CharWarsPlugin.getDefault().getLog();
		Status status = new Status(Status.ERROR, CharWarsPlugin.PLUGIN_ID, Status.OK, message, exception);
		logger.log(status);
	}
}
