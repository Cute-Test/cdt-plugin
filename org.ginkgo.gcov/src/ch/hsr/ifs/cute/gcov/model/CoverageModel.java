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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;

/**
 * @author Emanuel Graf IFS
 *
 */
public class CoverageModel {
	
	private Map<IFile, File> fileMap = new TreeMap<IFile, File>(new Comparator<IFile>() {

		public int compare(IFile o1, IFile o2) {
			return o1.getFullPath().toString().compareTo(o2.getFullPath().toString());
		}});
	
	public File addFileToModel(IFile file) {
		File f = new File(file);
		fileMap.put(file, f);
		return f;
		
	}

	public File getModelForFile(IFile file) {
		return fileMap.get(file);
	}
	
	public File removeFileFromModel(File file) {
		return removeFileFromModel(file.getFile());
	}
	
	public File removeFileFromModel(IFile file) {
		return fileMap.remove(file);
	}
	
	public void clearModel() {
		fileMap.clear();
	}

}
