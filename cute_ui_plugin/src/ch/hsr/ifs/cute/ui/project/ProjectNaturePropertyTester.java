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

	/**
	 * 
	 */
	public ProjectNaturePropertyTester() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		System.out.println(receiver);
		if (property.equals("projectNature1")) { //$NON-NLS-1$
			try {
				if(receiver instanceof FileEditorInput) {
					FileEditorInput fei = (FileEditorInput)receiver;
					receiver = fei.getFile(); //The IResource part will take care of the rest.
				}
				if(receiver instanceof IResource){
					IResource res = (IResource) receiver;
					IProject proj = res.getProject();
					boolean result=proj != null;
					boolean result1=proj.isAccessible();
					boolean result2= proj.hasNature(toString(expectedValue));
					return result && result1 && result2;
				}
				if(receiver instanceof IBinary){
					IBinary bin=(IBinary)receiver;
					IProject proj = bin.getCProject().getProject();
					boolean result= proj!= null;
					boolean result1= proj!=null ? proj.isAccessible(): false;
					boolean result2= proj!=null ? proj.hasNature(toString(expectedValue)):false;
					return result && result1 && result2;
				}
				
			} catch (CoreException e) {
				return false;
			}
		}
		
		return false;
	}

	/**
	 * Converts the given expected value to a <code>String</code>.
	 * 
	 * @param expectedValue
	 *            the expected value (may be <code>null</code>).
	 * @return the empty string if the expected value is <code>null</code>,
	 *         otherwise the <code>toString()</code> representation of the
	 *         expected value
	 */
	protected String toString(Object expectedValue) {
		return expectedValue == null ? "" : expectedValue.toString(); //$NON-NLS-1$
	}	
		
}
//@see org.eclipse.core.internal.propertytester.ResourcePropertyTester