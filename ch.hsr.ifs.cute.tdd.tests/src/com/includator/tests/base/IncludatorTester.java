/*******************************************************************************
 * Copyright (c) 2010, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.tdd.ui.tests.Activator;




public class IncludatorTester {

	enum MatcherState{skip, inTest, inSource, inExpectedResult}

	private static final String classRegexp = "//#(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String fileRegexp = "//@(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$

	public static Test suite(final String name, final String file) throws Exception {
		BufferedReader in = createReader(file);

		ArrayList<SourceFileTest> testCases = createTests(in);
		in.close();
		return createSuite(testCases, name);
	}

	protected static BufferedReader createReader(final String file) throws IOException {
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path(file);
		URL url = FileLocator.toFileURL(FileLocator.find(bundle, path, null));
		String file2 = url.getFile();
		return new BufferedReader(new FileReader(file2));
	}

	private static ArrayList<SourceFileTest> createTests(final BufferedReader inputReader) throws Exception {

		String line;
		Vector<TestSourceFile> files = new Vector<TestSourceFile>();
		TestSourceFile actFile = null;
		MatcherState matcherState = MatcherState.skip;
		ArrayList<SourceFileTest> testCases = new ArrayList<SourceFileTest>();
		String testName = null;
		String className = null;
		boolean bevorFirstTest = true;

		while ((line = inputReader.readLine()) != null){

			if(lineMatchesBeginOfTest(line)) {
				if(!bevorFirstTest) {
					SourceFileTest test = createTestClass(className, testName, files);
					testCases.add(test);
					files = new Vector<TestSourceFile>();
					className = null;
					testName = null;
				}
				matcherState = MatcherState.inTest;
				testName = getNameOfTest(line);
				bevorFirstTest = false;
				continue;
			}	else if (lineMatchesBeginOfResult(line)) {
				matcherState = MatcherState.inExpectedResult;
				continue;
			}else if (lineMatchesFileName(line)) {
				matcherState = MatcherState.inSource;
				actFile = new TestSourceFile(getFileName(line));
				files.add(actFile);
				continue;
			}else if(lineMatchesClassName(line)) {
				className = getNameOfClass(line);
				continue;
			}

			switch(matcherState) {
			case inSource:
				if(actFile != null) {
					actFile.addLineToSource(line);
				}
				break;
			case inExpectedResult:
				if(actFile != null) {
					actFile.addLineToExpectedSource(line);
				}
				break;
			default:
				break;
			}
		}
		SourceFileTest test = createTestClass(className, testName, files);
		testCases.add(test);
		return testCases;
	}



	private static SourceFileTest createTestClass(final String className, final String testName, final Vector<TestSourceFile> files) throws Exception {

		try {
			Class<?> refClass = Class.forName(className);
			Class<?> paratypes[] = new Class[2];
			paratypes[0] = testName.getClass();
			paratypes[1] = files.getClass();
			Constructor<?> ct = refClass.getConstructor(paratypes);
			Object arglist[] = new Object[2];
			arglist[0] = testName;
			arglist[1] = files;
			SourceFileTest test = (SourceFileTest) ct.newInstance(arglist);
			for (TestSourceFile file : files) {
				TextSelection sel = file.getSelection();
				if(sel != null) {
					test.setFileWithSelection(file.getName());
					test.setSelection(sel);
					break;
				}
			}
			return test;
		} catch (ClassNotFoundException e) {
			throw new Exception(Messages.getString("IncludatorTester.UnknownTestClass") + " Name: " + className); //$NON-NLS-1$
		} catch (SecurityException e) {
			throw new Exception(Messages.getString("IncludatorTester.SecurityException"), e); //$NON-NLS-1$
		} catch (NoSuchMethodException e) {
			throw new Exception(Messages.getString("IncludatorTester.ConstructorError")); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			throw new Exception(Messages.getString("IncludatorTester.IllegalArgument"), e); //$NON-NLS-1$
		} catch (InstantiationException e) {
			throw new Exception(Messages.getString("IncludatorTester.InstantiationException"), e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			throw new Exception(Messages.getString("IncludatorTester.IllegalAccessException"), e); //$NON-NLS-1$
		} catch (InvocationTargetException e) {
			throw new Exception(Messages.getString("IncludatorTester.InvocationTargetException"), e); //$NON-NLS-1$
		}
	}

	private static String getFileName(final String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(fileRegexp, line);
		if(matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return null;
		}
	}

	private static String getNameOfClass(final String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(classRegexp, line);
		if(matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return null;
		}
	}

	private static boolean lineMatchesBeginOfTest(final String line) {
		return createMatcherFromString(testRegexp, line).find();
	}

	private static boolean lineMatchesClassName(final String line) {
		return createMatcherFromString(classRegexp, line).find();
	}

	private static boolean lineMatchesFileName(final String line) {
		return createMatcherFromString(fileRegexp, line).find();
	}

	protected static Matcher createMatcherFromString(final String pattern, final String line) {
		return Pattern.compile(pattern).matcher(line);
	}

	private static String getNameOfTest(final String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(testRegexp, line);
		if(matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return Messages.getString("IncludatorTester.NotNamed"); //$NON-NLS-1$
		}
	}

	private static boolean lineMatchesBeginOfResult(final String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}

	private static TestSuite createSuite(final ArrayList<SourceFileTest> testCases, final String name) {
		TestSuite suite = new TestSuite(name);
		Iterator<SourceFileTest> it = testCases.iterator();
		while(it.hasNext()) {
			SourceFileTest subject =it.next();
			suite.addTest(subject);
		}
		return suite;
	}
}

