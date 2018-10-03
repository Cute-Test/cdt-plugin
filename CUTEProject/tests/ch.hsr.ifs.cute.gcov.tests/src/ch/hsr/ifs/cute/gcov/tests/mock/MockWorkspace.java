/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.tests.mock;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * @author Emanuel Graf IFS
 *
 */

public class MockWorkspace implements IWorkspace {

    List<IWorkspaceRunnable> actions = new ArrayList<>();

    @Override
    public void addResourceChangeListener(IResourceChangeListener listener) {
        throw new NotYetImplementedException();
    }

    @Override
    public void addResourceChangeListener(IResourceChangeListener listener, int eventMask) {
        throw new NotYetImplementedException();
    }

    @Override
    public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public ISavedState addSaveParticipant(String pluginId, ISaveParticipant participant) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void build(int kind, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void checkpoint(boolean build) {
        throw new NotYetImplementedException();
    }

    @Override
    public IProject[][] computePrerequisiteOrder(IProject[] projects) {
        throw new NotYetImplementedException();
    }

    @Override
    public ProjectOrder computeProjectOrder(IProject[] projects) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void deleteMarkers(IMarker[] markers) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void forgetSavedTree(String pluginId) {
        throw new NotYetImplementedException();
    }

    @Override
    public IFilterMatcherDescriptor[] getFilterMatcherDescriptors() {
        throw new NotYetImplementedException();
    }

    @Override
    public IFilterMatcherDescriptor getFilterMatcherDescriptor(String filterMatcherId) {
        throw new NotYetImplementedException();
    }

    @Override
    public IProjectNatureDescriptor[] getNatureDescriptors() {
        throw new NotYetImplementedException();
    }

    @Override
    public IProjectNatureDescriptor getNatureDescriptor(String natureId) {
        throw new NotYetImplementedException();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getDanglingReferences() {
        throw new NotYetImplementedException();
    }

    @Override
    public IWorkspaceDescription getDescription() {
        throw new NotYetImplementedException();
    }

    @Override
    public IWorkspaceRoot getRoot() {
        throw new NotYetImplementedException();
    }

    @Override
    public IResourceRuleFactory getRuleFactory() {
        throw new NotYetImplementedException();
    }

    @Override
    public ISynchronizer getSynchronizer() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isAutoBuilding() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isTreeLocked() {
        throw new NotYetImplementedException();
    }

    @Override
    public IProjectDescription loadProjectDescription(IPath projectDescriptionFile) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IProjectDescription loadProjectDescription(InputStream projectDescriptionFile) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IProjectDescription newProjectDescription(String projectName) {
        throw new NotYetImplementedException();
    }

    @Override
    public void removeResourceChangeListener(IResourceChangeListener listener) {
        throw new NotYetImplementedException();
    }

    @Override
    public void removeSaveParticipant(Plugin plugin) {
        throw new NotYetImplementedException();
    }

    @Override
    public void removeSaveParticipant(String pluginId) {
        throw new NotYetImplementedException();
    }

    @Override
    public void run(IWorkspaceRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException {
        actions.add(action);
        action.run(new NullProgressMonitor());
    }

    @Override
    public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void run(ICoreRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();

    }

    @Override
    public void run(ICoreRunnable action, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();

    }

    @Override
    public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public void setDescription(IWorkspaceDescription description) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public String[] sortNatureSet(String[] natureIds) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateEdit(IFile[] files, Object context) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateLinkLocation(IResource resource, IPath location) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateLinkLocationURI(IResource resource, URI location) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateName(String segment, int typeMask) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateNatureSet(String[] natureIds) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validatePath(String path, int typeMask) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateProjectLocation(IProject project, IPath location) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateProjectLocationURI(IProject project, URI location) {
        throw new NotYetImplementedException();
    }

    @Override
    public IPathVariableManager getPathVariableManager() {
        throw new NotYetImplementedException();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        throw new NotYetImplementedException();
    }

    @Override
    public IStatus validateFiltered(IResource resource) {
        throw new NotYetImplementedException();
    }

    @Override
    public void build(IBuildConfiguration[] buildConfigs, int kind, boolean buildReferences, IProgressMonitor monitor) throws CoreException {
        throw new NotYetImplementedException();
    }

    @Override
    public IBuildConfiguration newBuildConfig(String projectName, String configName) {
        throw new NotYetImplementedException();
    }

}
