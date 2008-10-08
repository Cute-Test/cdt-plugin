package ch.hsr.ifs.cutelauncher.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cutelauncher.ConsolePatternListener;
import ch.hsr.ifs.cutelauncher.event.ConsoleEventParser;
import ch.hsr.ifs.cutelauncher.event.CuteConsoleEventParser;
import ch.hsr.ifs.cutelauncher.test.internal.console.FileInputTextConsole;

public abstract class ConsoleTest extends TestCase {
	private ConsoleEventParser consoleEventParser;
	protected String filePathRoot;
	
	protected FileInputTextConsole tc;
	protected ConsolePatternListener cpl;

	@Override
	protected void setUp() throws Exception {
		useCUTE();
		prepareTest();
	}
	
	@Override
	protected void tearDown() throws Exception {
		tc.removePatternMatchListener(cpl);
		tc.end();
	}
	
	private void useCUTE() {
		consoleEventParser = new CuteConsoleEventParser();
		filePathRoot = "testDefs/cute/";
	}


	private void prepareTest() {
		tc = getConsole();
		cpl = new ConsolePatternListener(consoleEventParser);
		addTestEventHandler(cpl);
		tc.addPatternMatchListener(cpl);
		tc.startTest();
		try {
		Job.getJobManager().join(tc, new NullProgressMonitor());
		} catch (InterruptedException e){
			/* org.eclipse.core.internal.jobs.JobManager.join doesn't catch from sleep*/
		}
	}

	protected FileInputTextConsole getConsole() {
		return new FileInputTextConsole(fullFilePath());
	}
	
	protected abstract String getInputFilePath();
	
	protected abstract void addTestEventHandler(ConsolePatternListener lis) ;

	protected String firstConsoleLine() throws CoreException {
		Bundle bundle = TestPlugin.getDefault().getBundle();
		Path path = new Path(fullFilePath());
		try {
			String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
			BufferedReader br = new BufferedReader(new FileReader(file2));
			return br.readLine();
		}catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, 0,e.getMessage(), e));
		}
	}

	private String fullFilePath() {
		return filePathRoot + getInputFilePath();
	}
}