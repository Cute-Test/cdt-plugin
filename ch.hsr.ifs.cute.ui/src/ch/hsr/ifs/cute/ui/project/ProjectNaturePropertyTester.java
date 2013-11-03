/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Emanuel Graf
 * 
 */
public class ProjectNaturePropertyTester extends PropertyTester {

	public ProjectNaturePropertyTester() {
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals("projectNature1")) {
			try {
				if (receiver instanceof FileEditorInput) {
					FileEditorInput fei = (FileEditorInput) receiver;
					receiver = fei.getFile(); // The IResource part will take care of the rest.
				}
				if (receiver instanceof IResource) {
					IResource res = (IResource) receiver;
					IProject proj = res.getProject();
					return isCuteProject(expectedValue, proj);
				}
				if (receiver instanceof IBinary) {
					IBinary bin = (IBinary) receiver;
					IProject proj = bin.getCProject().getProject();
					return isCuteProject(expectedValue, proj);
				}
				if (receiver instanceof ICProject) {
					ICProject res = (ICProject) receiver;
					IProject proj = res.getProject();
					return isCuteProject(expectedValue, proj);
				}

			} catch (CoreException e) {
				return false;
			}
		}
		return false;
	}

	public boolean isCuteProject(Object expectedValue, IProject proj) throws CoreException {
		return proj != null && proj.isAccessible() && proj.hasNature(toString(expectedValue));
	}

	protected String toString(Object expectedValue) {
		return expectedValue == null ? "" : expectedValue.toString();
	}

}
