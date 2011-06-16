/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.testinfrastructure;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import com.includator.tests.base.TestSourceFile;

/**
 * 
 * This class is an adaptation of the Parameterized class from JUnit4.
 * 
 * 
 */
public class RTSTest extends Suite {

	private class RTSTestRunner extends BlockJUnit4ClassRunner {

		private final String testName;
		private final ArrayList<TestSourceFile> testFiles;

		RTSTestRunner(Class<?> type, String testName, ArrayList<TestSourceFile> testFiles) throws InitializationError {
			super(type);
			this.testName = testName;
			this.testFiles = testFiles;

		}

		@Override
		public Object createTest() throws Exception {
			return getTestClass().getOnlyConstructor().newInstance(computeParams());
		}

		private Object[] computeParams() throws Exception {
			try {
				return new Object[] { testName, testFiles };
			} catch (ClassCastException e) {
				throw new Exception(String.format("%s.%s() must return a Collection of arrays.", getTestClass().getName(), getParametersMethod(getTestClass()).getName()));
			}
		}

		@Override
		protected String getName() {
			return testName;
		}

		@Override
		protected String testName(final FrameworkMethod method) {
			return String.format("%s[%s]", method.getName(), getName());
		}

		@Override
		protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

		@Override
		protected Statement classBlock(RunNotifier notifier) {
			return childrenInvoker(notifier);
		}
	}

	private final ArrayList<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public RTSTest(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner> emptyList());
		final Map<String, ArrayList<TestSourceFile>> parametersList = getParametersList(getTestClass());
		TreeMap<String, ArrayList<TestSourceFile>> sorted = new TreeMap<String, ArrayList<TestSourceFile>>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return parametersList.get(o1).get(0).getName().compareTo(parametersList.get(o2).get(0).getName());
			}
		});
		sorted.putAll(parametersList);
		for (Entry<String, ArrayList<TestSourceFile>> testCase : sorted.entrySet()) {
			runners.add(new RTSTestRunner(getTestClass().getJavaClass(), testCase.getKey(), testCase.getValue()));
		}
	}
	

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private Map<String, ArrayList<TestSourceFile>> getParametersList(TestClass klass) throws Throwable {
		return (Map<String, ArrayList<TestSourceFile>>) getParametersMethod(klass).invokeExplosively(null, klass.getJavaClass());
	}

	private FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
		List<FrameworkMethod> methods = testClass.getAnnotatedMethods(RTSTestCases.class);

		for (FrameworkMethod each : methods) {
			int modifiers = each.getMethod().getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
				return each;
		}

		throw new Exception("No public static parameters method on class " + testClass.getName());
	}
}
