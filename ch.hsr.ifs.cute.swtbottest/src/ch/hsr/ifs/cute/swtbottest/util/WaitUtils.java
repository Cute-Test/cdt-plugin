package ch.hsr.ifs.cute.swtbottest.util;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public final class WaitUtils {

	private static final int INDEXER_TIMEOUT = 5000;
	private static final int BOT_TIMEOUT = 500;

	private static final String CPP_INDEXER_JOBNAME = "C/C++ Indexer";
	private static final String BUILD_PROJECT_JOBNAME = "Build Project";

	/**
	 * Wait for the the project build job to finish.
	 */
	public static final void waitForBuildJob(final SWTWorkbenchBot bot) {
		while (Arrays.stream(Job.getJobManager().find(null))
				.filter(job -> job.getName().equals(BUILD_PROJECT_JOBNAME))
				.anyMatch(job -> job.getState() != Job.NONE)) {
			bot.sleep(BOT_TIMEOUT);
		}
	}

	/**
	 * Wait for the indexer to finish indexing the files.
	 */
	public static void waitForIndexer(final SWTWorkbenchBot bot) {
		while(!Arrays.stream(Job.getJobManager().find(null))
				.anyMatch(job -> job.getName().equals(CPP_INDEXER_JOBNAME))) {
			bot.sleep(BOT_TIMEOUT);
		}

		CCorePlugin.getIndexManager().joinIndexer(INDEXER_TIMEOUT, new NullProgressMonitor());
	}
}
