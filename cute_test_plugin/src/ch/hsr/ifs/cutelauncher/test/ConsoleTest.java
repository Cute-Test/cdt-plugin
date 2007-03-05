package ch.hsr.ifs.cutelauncher.test;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.cutelauncher.CutePatternListener;
import ch.hsr.ifs.cutelauncher.test.internal.console.FileInputTextConsole;

public abstract class ConsoleTest extends TestCase {

	protected FileInputTextConsole tc;
	protected CutePatternListener cpl;

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {
		tc = getConsole();
		cpl = new CutePatternListener();
		addTestEventHandler(cpl);
		tc.addPatternMatchListener(cpl);
		tc.startTest();
		Job.getJobManager().join(tc, new NullProgressMonitor());
		
	}

	protected FileInputTextConsole getConsole() {
		return new FileInputTextConsole(getInputFile());
	}
	
	protected abstract String getInputFile();

	protected abstract void addTestEventHandler(CutePatternListener lis) ;

	@Override
	protected void tearDown() throws Exception {
		tc.removePatternMatchListener(cpl);
		tc.end();
	}

}