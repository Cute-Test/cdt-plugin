/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf & Guido Zgraggen- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.core.test.modelbuilder;

import java.util.Vector;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.core.test.ConsoleTest;
import ch.hsr.ifs.test.framework.TestFrameworkPlugin;
import ch.hsr.ifs.test.framework.launch.ConsolePatternListener;
import ch.hsr.ifs.test.framework.model.ModellBuilder;
import ch.hsr.ifs.test.framework.model.TestCase;
import ch.hsr.ifs.test.framework.model.TestElement;
import ch.hsr.ifs.test.framework.model.TestFailure;
import ch.hsr.ifs.test.framework.model.TestResult;
import ch.hsr.ifs.test.framework.model.TestSession;
import ch.hsr.ifs.test.framework.model.TestSuite;

/**
 * @author Emanuel Graf
 *
 */
public class ModelBuilderTest extends ConsoleTest {
	
	private static final String SEPARATOR = ", "; //$NON-NLS-1$
	private String inputFile;

	public ModelBuilderTest(String inputFile) {
		super();
		this.inputFile = inputFile;
	}
	
	public static Test suite(String inputFile) {
		String testName = inputFile.split("\\.")[0]; //$NON-NLS-1$
		junit.framework.TestSuite suite = new junit.framework.TestSuite(testName);
		suite.addTest(new ModelBuilderTest(inputFile));
		return suite;
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new ModellBuilder(new Path(""))); //$NON-NLS-1$
	}
	
	protected String getExpected() throws CoreException {
		return firstConsoleLine();
	}

	@Override
	public String getName() {
		return inputFile;
	}

	@Override
	protected void runTest() throws Throwable {
		TestSession session = TestFrameworkPlugin.getModel().getSession();
		assertEquals(getExpected(), getSessionString(session));
	}

	private String getSessionString(TestSession session) {
		StringBuffer sb = new StringBuffer();
		sb.append("Session{"); //$NON-NLS-1$
		Vector<TestElement> rootElements = session.getRootElements();
		writeElements(sb, rootElements);
		sb.append('}');
		return sb.toString();
	}

	private void writeTestCase(TestCase tcase, StringBuffer sb) {
		sb.append("Testcase("); //$NON-NLS-1$
		sb.append(tcase.getName());
		sb.append(SEPARATOR);
		sb.append(tcase.getStatus().toString());
		sb.append(SEPARATOR);
		sb.append(tcase.getFile());
		sb.append(SEPARATOR);
		sb.append(tcase.getLineNumber());
		sb.append(SEPARATOR);
		writeTestResult(tcase.getResult(), sb);
		sb.append(')');
	}

	private void writeTestResult(TestResult result, StringBuffer sb) {
		sb.append("Result("); //$NON-NLS-1$
		sb.append(result.getMsg());
		if (result instanceof TestFailure) {
			TestFailure failure = (TestFailure) result;
			sb.append(SEPARATOR);
			sb.append(failure.getExpected());
			sb.append(SEPARATOR);
			sb.append(failure.getWas());
		}
		sb.append(')');
		
	}

	private void writeSuite(TestSuite suite, StringBuffer sb) {
		sb.append("Suite("); //$NON-NLS-1$
		sb.append(suite.getName());
		sb.append(SEPARATOR);
		sb.append(suite.getStatus().toString());
		sb.append(SEPARATOR);
		sb.append(suite.getTotalTests());
		sb.append(SEPARATOR);
		sb.append(suite.getRun());
		sb.append(SEPARATOR);
		sb.append(suite.getSuccess());
		sb.append(SEPARATOR);
		sb.append(suite.getFailure());
		sb.append(SEPARATOR);
		sb.append(suite.getError());
		sb.append("){"); //$NON-NLS-1$
		Vector<TestElement> elements = suite.getElements();
		writeElements(sb, elements);
		sb.append('}');
		
	}

	private void writeElements(StringBuffer sb, Vector<TestElement> elements) {
		for (TestElement element : elements) {
			if (element instanceof TestSuite) {
				TestSuite suite1 = (TestSuite) element;
				writeSuite(suite1, sb);
			}else if (element instanceof TestCase) {
				TestCase tcase = (TestCase) element;
				writeTestCase(tcase, sb);
			}
		}
	}

	@Override
	public String getInputFilePath() {
		return "modelBuilderTests/" + inputFile; //$NON-NLS-1$
	}

}