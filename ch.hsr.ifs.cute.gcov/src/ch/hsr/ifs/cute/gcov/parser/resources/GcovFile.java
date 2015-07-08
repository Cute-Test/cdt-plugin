/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser.resources;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.ui.FileFinderVisitor;

/**
 * @author Thomas Corbat IFS
 * 
 */
public class GcovFile {

	private final IFile file;
	private final String suffix;
	private static final String SUFFIX_DELIMITER = ".";
	private static final String GCOV_SUFFIX = SUFFIX_DELIMITER + "gcov";
	private static final String GCNO_SUFFIX = SUFFIX_DELIMITER + "gcno";
	private static final HashSet<String> knownSuffixes = new HashSet<String>();

	static {
		knownSuffixes.add(".cpp");
		knownSuffixes.add(".c");
	}

	protected GcovFile(IFile file, String suffix) {
		this.file = file;
		this.suffix = suffix;
	}

	public IFile getGcnoFile() {
		return findFile(file.getProject(), getGcnoFilename());
	}

	public IFile getGcovFile() {
		return findFile(file.getProject(), getGcovFilename());
	}

	public static GcovFile create(IFile file) {
		if (file != null) {
			String suffix = getLowercaseSuffix(file);
			if (knownSuffixes.contains(suffix)) {
				return new GcovFile(file, suffix);
			}
		}
		return null;
	}

	private static String getLowercaseSuffix(IFile file) {
		String suffix = SUFFIX_DELIMITER + file.getFileExtension();
		return suffix.toLowerCase();
	}

	public String getFileName() {
		return file.getName();
	}

	public IFile getFile() {
		return file;
	}

	public String getGcnoFilename() {
		return getFileName().replace(suffix, GCNO_SUFFIX);
	}

	public String getGcovFilename() {
		return getFileName().concat(GCOV_SUFFIX);
	}

	private IFile findFile(IProject project, String fileName) {
		FileFinderVisitor visitor = new FileFinderVisitor(fileName);
		try {
			project.accept(visitor);
			return visitor.getFile();
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return file.toString();
	}
}
