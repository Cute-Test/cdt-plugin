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

import org.eclipse.core.resources.IFile;

/**
 * @author egraf
 *
 */
public class TestCase implements TestElement {
	
	private TestStatus status;
	
	private String name;
	
	private IFile file;
	
	private int lineNumber = -1;
	
	private String msg = "";
	
	private TestSuite suite = null;

	public TestCase(String name) {
		super();
		this.name = name;
		status = TestStatus.running;
	}

	public TestElement getSuite() {
		return suite;
	}

	public void setSuite(TestSuite suite) {
		this.suite = suite;
	}

	public IFile getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public TestStatus getStatus() {
		return status;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public String getMessage() {
		return msg;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public void endTest(IFile file, int lineNumber, String msg, TestStatus status) {
		this.file = file;
		this.lineNumber = lineNumber;
		this.msg = msg;
		this.status = status;
		suite.endTest(this);
	}
	
	

}
