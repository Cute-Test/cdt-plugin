/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.test.mock;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Emanuel Graf IFS
 *
 */
public class MockMarker implements IMarker {
	
	private final String type;
	private final IResource res;
	private Map<String, Object> attributes = new HashMap<String, Object>();;

	public MockMarker(String type, IResource resource) {
		super();
		this.type = type;
		res = resource;
	}

	public void delete() throws CoreException {
		throw new NotYetImplementedException();
	}

	public boolean exists() {
		throw new NotYetImplementedException();
	}

	public Object getAttribute(String attributeName) throws CoreException {
		return attributes.get(attributeName);
	}

	public int getAttribute(String attributeName, int defaultValue) {
		Object ret;
		try {
			ret = getAttribute(attributeName);
		} catch (CoreException e) {
			return defaultValue;
		}
		if (ret instanceof Integer) {
			return (Integer) ret;
		}else {
			return defaultValue;
		}
	}

	public String getAttribute(String attributeName, String defaultValue) {
		Object ret;
		try {
			ret = getAttribute(attributeName);
		} catch (CoreException e) {
			return defaultValue;
		}
		if (ret instanceof String) {
			return (String) ret;
		}else {
			return defaultValue;
		}
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getAttributes() throws CoreException {
		return attributes;
	}

	public Object[] getAttributes(String[] attributeNames) throws CoreException {
		throw new NotYetImplementedException();
	}

	public long getCreationTime() throws CoreException {
		throw new NotYetImplementedException();
	}

	public long getId() {
		throw new NotYetImplementedException();
	}

	public IResource getResource() {
		return res;
	}

	public String getType() throws CoreException {
		return type;
	}

	public boolean isSubtypeOf(String superType) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setAttribute(String attributeName, int value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setAttribute(String attributeName, Object value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setAttribute(String attributeName, boolean value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setAttributes(Map attributes) throws CoreException {
		this.attributes = attributes;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		throw new NotYetImplementedException();
	}

}
