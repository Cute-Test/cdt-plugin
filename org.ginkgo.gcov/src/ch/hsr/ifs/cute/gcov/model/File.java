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

import org.eclipse.core.resources.IFile;

/**
 * @author Emanuel Graf IFS
 *
 */
public class File {
	
	private IFile file;
	private List<Function> functions = new ArrayList<Function>();

	public File(IFile file) {
		super();
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}
	
	public String getFileName(){
		return file.getName();
	}
	
	public void addFunction(Function f) {
		functions.add(f);
		f.setFile(this);
	}
	
	public List<Function> getFunctions() {
		return functions;
	}

}
