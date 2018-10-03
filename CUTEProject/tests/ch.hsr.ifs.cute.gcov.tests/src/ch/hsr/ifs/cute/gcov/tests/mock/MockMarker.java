/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.tests.mock;

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

    private final String        type;
    private final IResource     res;
    private Map<String, Object> attributes = new HashMap<>();

    public MockMarker(String type, IResource resource) {
        super();
        this.type = type;
        res = resource;
    }

    @Override
    public void delete() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean exists() {
        throw new NotYetImplementedException();
    }

    @Override
    public Object getAttribute(String attributeName) throws CoreException {
        return attributes.get(attributeName);
    }

    @Override
    public int getAttribute(String attributeName, int defaultValue) {
        Object ret;
        try {
            ret = getAttribute(attributeName);
        } catch (CoreException e) {
            return defaultValue;
        }
        if (ret instanceof Integer) {
            return (Integer) ret;
        } else {
            return defaultValue;
        }
    }

    @Override
    public String getAttribute(String attributeName, String defaultValue) {
        Object ret;
        try {
            ret = getAttribute(attributeName);
        } catch (CoreException e) {
            return defaultValue;
        }
        if (ret instanceof String) {
            return (String) ret;
        } else {
            return defaultValue;
        }
    }

    @Override
    public boolean getAttribute(String attributeName, boolean defaultValue) {
        throw new NotYetImplementedException();
    }

    @Override
    public Map<String, Object> getAttributes() throws CoreException {
        return attributes;
    }

    @Override
    public Object[] getAttributes(String[] attributeNames) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public long getCreationTime() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public long getId() {
        throw new NotYetImplementedException();
    }

    @Override
    public IResource getResource() {
        return res;
    }

    @Override
    public String getType() throws CoreException {
        return type;
    }

    @Override
    public boolean isSubtypeOf(String superType) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setAttribute(String attributeName, int value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setAttribute(String attributeName, Object value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setAttribute(String attributeName, boolean value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException {
        this.attributes = (Map<String, Object>) attributes;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        throw new NotYetImplementedException();
    }

}
