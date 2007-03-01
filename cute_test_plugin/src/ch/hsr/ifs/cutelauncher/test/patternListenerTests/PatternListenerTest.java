package ch.hsr.ifs.cutelauncher.test.patternListenerTests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.cutelauncher.CutePatternListener;
import ch.hsr.ifs.cutelauncher.test.internal.console.FileInputTextConsole;

public abstract class PatternListenerTest extends TestCase {

	protected FileInputTextConsole tc;
	protected CutePatternListener cpl;

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {
		tc = new FileInputTextConsole(getInputFile());
		cpl = new CutePatternListener();
		addTestEventHandler(cpl);
		tc.addPatternMatchListener(cpl);
		tc.startTest();
		Job.getJobManager().join(tc, new NullProgressMonitor());
		
	}
	
	protected abstract String getInputFile();

	protected abstract void addTestEventHandler(CutePatternListener lis) ;

	@Override
	protected void tearDown() throws Exception {
		tc.removePatternMatchListener(cpl);
		tc.end();
	}

}