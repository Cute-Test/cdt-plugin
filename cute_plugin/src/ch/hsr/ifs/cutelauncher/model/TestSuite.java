/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.model;

import java.util.Vector;

/**
 * @author egraf
 *
 */
public class TestSuite implements TestElement {
	
	private String name = "";
	
	private int totalTests = 0; 
	private int success = 0;
	private int failure = 0;
	private int error = 0;
	
	private TestStatus status;
	
	private Vector<TestCase> cases = new Vector<TestCase>();

	public TestSuite(String name, int totalTests, TestStatus status) {
		super();
		this.name = name;
		this.totalTests = totalTests;
		this.status = status;
	}

	public Vector<TestCase> getCases() {
		return cases;
	}

	public String getName() {
		return name;
	}

	public TestStatus getStatus() {
		return status;
	}
	
	public void add(TestCase tCase) {
		cases.add(tCase);
		tCase.setSuite(this);
	}
	
	protected void endTest(TestCase tCase) {
		switch(tCase.getStatus()) {
		case success:
			++success;
			break;
		case failure:
			++failure;
			break;
		case error:
			++error;
			break;
		}
	}
	
	private void setEndStatus() {
		for (TestCase tCase : cases) {
			switch (status) {
			case running:
				status = tCase.getStatus();
				break;
			case success:
				if(tCase.getStatus() != TestStatus.success) {
					status = tCase.getStatus();
				}
				break;
			case failure:
				if(tCase.getStatus() == TestStatus.error) {
					status = tCase.getStatus();
				}
				break;
			default:
				//nothing
			}
		}
	}

	public int getError() {
		return error;
	}

	public int getFailure() {
		return failure;
	}

	public int getSuccess() {
		return success;
	}

	public int getTotalTests() {
		return totalTests;
	}

	public int getRun() {
		return success + failure + error;
	}
	@Override
	public String toString() {
		return getName();
	}

	public void end() {
		setEndStatus();
		
	}

}
