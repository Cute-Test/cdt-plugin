/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.tests.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * @author Emanuel Graf IFS
 *
 */
@SuppressWarnings("restriction")
public class MockFile implements IFile {

    private final IPath         path;
    private final MockWorkspace mockWorkspace;
    private final List<IMarker> markers = new ArrayList<>();
    private final boolean       exists;
    private final String        contents;

    public MockFile(IPath path, boolean exists, String contents) {
        this.path = path;
        mockWorkspace = new MockWorkspace();
        this.exists = exists;
        this.contents = contents;
    }

    public MockFile(IPath path, String contents) {
        this(path, true, contents);
    }

    @Override
    public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getCharset() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getCharset(boolean checkImplicit) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getCharsetFor(Reader reader) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IContentDescription getContentDescription() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public InputStream getContents() throws CoreException {
        try {
            InputStream is = new ByteArrayInputStream(contents.getBytes("UTF-8"));
            return is;
        } catch (UnsupportedEncodingException e) {}
        return null;
    }

    @Override
    public InputStream getContents(boolean force) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public int getEncoding() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IPath getFullPath() {
        return path;
    }

    @Override
    public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getName() {
        return path.lastSegment();
    }

    @Override
    public boolean isReadOnly() {
        throw new NotYetImplementedException();
    }

    @Override
    public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setCharset(String newCharset) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void accept(IResourceVisitor visitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void clearHistory(IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IMarker createMarker(String type) throws CoreException {
        MockMarker marker = new MockMarker(type, this);
        markers.add(marker);
        return marker;
    }

    @Override
    public IResourceProxy createProxy() {
        throw new NotYetImplementedException();
    }

    @Override
    public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        if (!exists()) {
            String message = "File does not exist";
            throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, getFullPath(), message, null);
        }
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public IMarker findMarker(long id) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getFileExtension() {
        throw new NotYetImplementedException();
    }

    @Override
    public long getLocalTimeStamp() {
        throw new NotYetImplementedException();
    }

    @Override
    public IPath getLocation() {
        throw new NotYetImplementedException();
    }

    @Override
    public URI getLocationURI() {
        throw new NotYetImplementedException();
    }

    @Override
    public IMarker getMarker(long id) {
        throw new NotYetImplementedException();
    }

    @Override
    public long getModificationStamp() {
        throw new NotYetImplementedException();
    }

    @Override
    public IContainer getParent() {
        throw new NotYetImplementedException();
    }

    @Override
    public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String getPersistentProperty(QualifiedName key) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IProject getProject() {
        throw new NotYetImplementedException();
    }

    @Override
    public IPath getProjectRelativePath() {
        throw new NotYetImplementedException();
    }

    @Override
    public IPath getRawLocation() {
        throw new NotYetImplementedException();
    }

    @Override
    public URI getRawLocationURI() {
        throw new NotYetImplementedException();
    }

    @Override
    public ResourceAttributes getResourceAttributes() {
        throw new NotYetImplementedException();
    }

    @Override
    public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public Object getSessionProperty(QualifiedName key) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public int getType() {
        throw new NotYetImplementedException();
    }

    @Override
    public IWorkspace getWorkspace() {
        return mockWorkspace;
    }

    @Override
    public boolean isAccessible() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isDerived() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isDerived(int options) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isHidden() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isHidden(int options) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isLinked() {
        throw new NotYetImplementedException();
    }

    public boolean isGroup() {
        throw new NotYetImplementedException();
    }

    public boolean hasFilters() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isLinked(int options) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isLocal(int depth) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isPhantom() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isSynchronized(int depth) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isTeamPrivateMember() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isTeamPrivateMember(int options) {
        throw new NotYetImplementedException();
    }

    @Override
    public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void revertModificationStamp(long value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setDerived(boolean isDerived) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setHidden(boolean isHidden) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public long setLocalTimeStamp(long value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new NotYetImplementedException();
    }

    @Override
    public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void touch(IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        throw new NotYetImplementedException();
    }

    public List<IMarker> getMarkers() {
        return markers;
    }

    @Override
    public boolean isVirtual() {
        throw new NotYetImplementedException();
    }

    @Override
    public IPathVariableManager getPathVariableManager() {
        throw new NotYetImplementedException();
    }

    public boolean isFiltered() {
        throw new NotYetImplementedException();
    }

    @Override
    public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
        throw new NotYetImplementedException();

    }

}
