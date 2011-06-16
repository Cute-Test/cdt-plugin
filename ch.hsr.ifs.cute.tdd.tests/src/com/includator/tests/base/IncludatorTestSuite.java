/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.base;

import junit.framework.TestSuite;

public abstract class IncludatorTestSuite extends TestSuite {

	private String prefix;

	protected abstract void collectTests() throws Exception;

	public IncludatorTestSuite() {
		setName(getClass().getSimpleName());
	}
	
	protected TestSuite makeSuite() throws Exception {
		collectTests();
		return this;
	}

	protected void quickAddTests(String testName) throws Exception {
		addTest(IncludatorTester.suite(testName, prefix + testName + ".rts"));

	}

	protected void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
