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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		File other = (File) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
	
	

}
