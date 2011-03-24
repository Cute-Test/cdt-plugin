/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
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
