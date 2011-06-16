/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - Adapted for Includator
 *     Copied from Parameterized.java in JUnit4
 ******************************************************************************/
package com.includator.tests.base;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.tdd.ui.tests.Activator;

import com.includator.tests.testinfrastructure.RTSTest;
import com.includator.tests.testinfrastructure.RTSTestCases;
import com.includator.tests.testinfrastructure.RunFor;

@RunWith(RTSTest.class)
public abstract class JUnit4IncludatorTest extends IncludatorTest {

	private static final String TEST_PACKAGE_PREFIX = "ch.hsr.eclipse.cdt.ui.tests.";
	private static final String TEST_RESOURCE_PREFIX = "resources/"; //$NON-NLS-1$

	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String fileRegexp = "//@(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$

	private enum MatcherState {
		skip, inTest, inSource, inExpectedResult
	}

	public JUnit4IncludatorTest(String name, ArrayList<TestSourceFile> files) {
		super(name, new Vector<TestSourceFile>(files));
	}

	@RTSTestCases
	public static Map<String, ArrayList<TestSourceFile>> testCases(Class<? extends JUnit4IncludatorTest> testClass) throws Exception {

		String completeRTSPath = null;

		RunFor runForAnnotation = testClass.getAnnotation(RunFor.class);
		if (runForAnnotation != null) {
			completeRTSPath = runForAnnotation.rtsFile();
		} else {
			completeRTSPath = createDefaultRTSPath(testClass);
		}

		BufferedReader in = createReader(completeRTSPath);
		Map<String, ArrayList<TestSourceFile>> testCases = createTests(in);
		return testCases;
	}

	private static String createDefaultRTSPath(Class<? extends JUnit4IncludatorTest> testClass) {
		StringBuffer completeRTSPath = new StringBuffer(TEST_RESOURCE_PREFIX);
		completeRTSPath.append(testClass.getName().substring(TEST_PACKAGE_PREFIX.length()).replace(".", "/")).append(".rts");
		return completeRTSPath.toString();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	protected static BufferedReader createReader(final String file) throws IOException {
		Bundle bundle = Activator.getDefault().getBundle();

		Path path = new Path(file);
		final URL filePath = FileLocator.find(bundle, path, null);
		if (filePath == null) {
			throw new FileNotFoundException(file);
		}
		URL url = FileLocator.toFileURL(filePath);
		String file2 = url.getFile();
		return new BufferedReader(new FileReader(file2));
	}

	private static Map<String, ArrayList<TestSourceFile>> createTests(final BufferedReader inputReader) throws Exception {
		Map<String, ArrayList<TestSourceFile>> testCases = new TreeMap<String, ArrayList<TestSourceFile>>();

		String line;
		ArrayList<TestSourceFile> files = new ArrayList<TestSourceFile>();
		TestSourceFile actFile = null;
		MatcherState matcherState = MatcherState.skip;
		String testName = null;
		boolean bevorFirstTest = true;

		while ((line = inputReader.readLine()) != null) {

			if (lineMatchesBeginOfTest(line)) {
				if (!bevorFirstTest) {
					testCases.put(testName, files);
					files = new ArrayList<TestSourceFile>();
					testName = null;
				}
				matcherState = MatcherState.inTest;
				testName = getNameOfTest(line);
				bevorFirstTest = false;
				continue;
			} else if (lineMatchesBeginOfResult(line)) {
				matcherState = MatcherState.inExpectedResult;
				continue;
			} else if (lineMatchesFileName(line)) {
				matcherState = MatcherState.inSource;
				actFile = new TestSourceFile(getFileName(line));
				files.add(actFile);
				continue;
			}

			switch (matcherState) {
			case inSource:
				if (actFile != null) {
					actFile.addLineToSource(line);
				}
				break;
			case inExpectedResult:
				if (actFile != null) {
					actFile.addLineToExpectedSource(line);
				}
				break;
			}
		}
		testCases.put(testName, files);

		return testCases;
	}

	private static String getFileName(final String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(fileRegexp, line);
		if (matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return null;
		}
	}

	private static boolean lineMatchesBeginOfTest(final String line) {
		return createMatcherFromString(testRegexp, line).find();
	}

	private static boolean lineMatchesFileName(final String line) {
		return createMatcherFromString(fileRegexp, line).find();
	}

	protected static Matcher createMatcherFromString(final String pattern, final String line) {
		return Pattern.compile(pattern).matcher(line);
	}

	private static String getNameOfTest(final String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(testRegexp, line);
		if (matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return Messages.getString("IncludatorTester.NotNamed"); //$NON-NLS-1$
		}
	}

	private static boolean lineMatchesBeginOfResult(final String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}

}
