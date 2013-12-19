/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.test.mock.FileInputTextConsole;

/**
 * @author Emanuel Graf IFS
 * 
 */
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

	private void prepareTest() throws OperationCanceledException, InterruptedException {
		tc = getConsole();
		cpl = new ConsolePatternListener(consoleEventParser);
		addTestEventHandler(cpl);
		tc.addPatternMatchListener(cpl);
		tc.startTest();
		Job.getJobManager().join(tc, new NullProgressMonitor());
	}

	protected FileInputTextConsole getConsole() {
		return new FileInputTextConsole(fullFilePath());
	}

	protected abstract String getInputFilePath();

	protected abstract void addTestEventHandler(ConsolePatternListener lis);

	protected String firstConsoleLine() throws CoreException, IOException {
		Bundle bundle = TestframeworkTestPlugin.getDefault().getBundle();
		Path path = new Path(fullFilePath());
		BufferedReader br = null;
		try {
			String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
			br = new BufferedReader(new FileReader(file2));
			return br.readLine();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	private String fullFilePath() {
		return filePathRoot + getInputFilePath();
	}
}