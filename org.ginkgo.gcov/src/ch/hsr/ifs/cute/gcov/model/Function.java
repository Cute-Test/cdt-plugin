/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emanuel Graf IFS
 *
 */
public class Function {
	
	private String name;
	private int called;
	private int execBlocks;
	private List<Line>lines = new ArrayList<Line>();
	private File file;
	
	public Function(String name, int called, int execBlocks) {
		super();
		this.name = name;
		this.called = called;
		this.execBlocks = execBlocks;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public int getCalled() {
		return called;
	}

	public int getExecBlocks() {
		return execBlocks;
	}

	public List<Line> getLines() {
		return lines;
	}
	
	public void addLine(Line l) {
		lines.add(l);
		l.setFunction(this);
	}

	@Override
	public String toString() {
		return name;
	}

	
}
