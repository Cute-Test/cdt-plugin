/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.test.mock;

import java.io.InputStream;
import java.io.Reader;
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
public class MockFile implements IFile {

	private final IPath path;
	private final MockWorkspace mockWorkspace;
	private final List<IMarker> markers = new ArrayList<IMarker>();
	private final boolean exists;

	public MockFile(IPath path, boolean exists) {
		this.path = path;
		mockWorkspace = new MockWorkspace();
		this.exists = exists;
	}

	public MockFile(IPath path) {
		this(path, true);
	}

	public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getCharset() throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getCharset(boolean checkImplicit) throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getCharsetFor(Reader reader) throws CoreException {
		throw new NotYetImplementedException();
	}

	public IContentDescription getContentDescription() throws CoreException {
		throw new NotYetImplementedException();
	}

	public InputStream getContents() throws CoreException {
		throw new NotYetImplementedException();
	}

	public InputStream getContents(boolean force) throws CoreException {
		throw new NotYetImplementedException();
	}

	public int getEncoding() throws CoreException {
		throw new NotYetImplementedException();
	}

	public IPath getFullPath() {
		return path;
	}

	public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getName() {
		return path.lastSegment();
	}

	public boolean isReadOnly() {
		throw new NotYetImplementedException();
	}

	public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setCharset(String newCharset) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public IMarker createMarker(String type) throws CoreException {
		MockMarker marker = new MockMarker(type, this);
		markers.add(marker);
		return marker;
	}

	public IResourceProxy createProxy() {
		throw new NotYetImplementedException();
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings("restriction")
	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		if (!exists()) {
			String message = "File does not exist"; //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, getFullPath(), message, null);
		}
	}

	public boolean exists() {
		return exists;
	}

	public IMarker findMarker(long id) throws CoreException {
		throw new NotYetImplementedException();
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new NotYetImplementedException();
	}

	public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getFileExtension() {
		throw new NotYetImplementedException();
	}

	public long getLocalTimeStamp() {
		throw new NotYetImplementedException();
	}

	public IPath getLocation() {
		throw new NotYetImplementedException();
	}

	public URI getLocationURI() {
		throw new NotYetImplementedException();
	}

	public IMarker getMarker(long id) {
		throw new NotYetImplementedException();
	}

	public long getModificationStamp() {
		throw new NotYetImplementedException();
	}

	public IContainer getParent() {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings("rawtypes")
	public Map getPersistentProperties() throws CoreException {
		throw new NotYetImplementedException();
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new NotYetImplementedException();
	}

	public IProject getProject() {
		throw new NotYetImplementedException();
	}

	public IPath getProjectRelativePath() {
		throw new NotYetImplementedException();
	}

	public IPath getRawLocation() {
		throw new NotYetImplementedException();
	}

	public URI getRawLocationURI() {
		throw new NotYetImplementedException();
	}

	public ResourceAttributes getResourceAttributes() {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings("rawtypes")
	public Map getSessionProperties() throws CoreException {
		throw new NotYetImplementedException();
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new NotYetImplementedException();
	}

	public int getType() {
		throw new NotYetImplementedException();
	}

	public IWorkspace getWorkspace() {
		return mockWorkspace;
	}

	public boolean isAccessible() {
		throw new NotYetImplementedException();
	}

	public boolean isDerived() {
		throw new NotYetImplementedException();
	}

	public boolean isDerived(int options) {
		throw new NotYetImplementedException();
	}

	public boolean isHidden() {
		throw new NotYetImplementedException();
	}

	public boolean isHidden(int options) {
		throw new NotYetImplementedException();
	}

	public boolean isLinked() {
		throw new NotYetImplementedException();
	}

	public boolean isGroup() {
		throw new NotYetImplementedException();
	}

	public boolean hasFilters() {
		throw new NotYetImplementedException();
	}

	public boolean isLinked(int options) {
		throw new NotYetImplementedException();
	}

	public boolean isLocal(int depth) {
		throw new NotYetImplementedException();
	}

	public boolean isPhantom() {
		throw new NotYetImplementedException();
	}

	public boolean isSynchronized(int depth) {
		throw new NotYetImplementedException();
	}

	public boolean isTeamPrivateMember() {
		throw new NotYetImplementedException();
	}

	public boolean isTeamPrivateMember(int options) {
		throw new NotYetImplementedException();
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void revertModificationStamp(long value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setDerived(boolean isDerived) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setHidden(boolean isHidden) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	public long setLocalTimeStamp(long value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setReadOnly(boolean readOnly) {
		throw new NotYetImplementedException();
	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
		throw new NotYetImplementedException();
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new NotYetImplementedException();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		throw new NotYetImplementedException();
	}

	public boolean contains(ISchedulingRule rule) {
		throw new NotYetImplementedException();
	}

	public boolean isConflicting(ISchedulingRule rule) {
		throw new NotYetImplementedException();
	}

	public List<IMarker> getMarkers() {
		return markers;
	}

	public boolean isVirtual() {
		throw new NotYetImplementedException();
	}

	public IPathVariableManager getPathVariableManager() {
		throw new NotYetImplementedException();
	}

	public boolean isFiltered() {
		throw new NotYetImplementedException();
	}

	public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new NotYetImplementedException();

	}

}
