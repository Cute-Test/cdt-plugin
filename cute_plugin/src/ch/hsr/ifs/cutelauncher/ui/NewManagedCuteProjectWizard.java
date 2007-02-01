/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule f�r Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.internal.ui.wizards.folderwizard.NewFolderWizardMessages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedCCProjectWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;

/**
 * @author egraf
 *
 */
public class NewManagedCuteProjectWizard extends NewManagedCCProjectWizard {

	IWorkspaceRoot fWorkspaceRoot= ResourcesPlugin.getWorkspace().getRoot();

	public NewManagedCuteProjectWizard() {
		super("CUTE Project", "New CUTE Test-Project.");
	}

	public NewManagedCuteProjectWizard(String title, String desc) {
		super(title, desc);
	}

	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		IFolder cuteFolder = createCuteSourceFolder(monitor);
		createTestSourceFolder(monitor);
		setIncludePaths(monitor, cuteFolder);
	}
	
	private void setIncludePaths(IProgressMonitor monitor, IFolder cuteFolder) {
		IManagedBuildInfo bInfo = null;
		bInfo = ManagedBuildManager.getBuildInfo(newProject);
		IManagedProject mProj = bInfo.getManagedProject();
		IConfiguration[] configs = mProj.getConfigurations();
		for (IConfiguration configuration : configs) {
			ITool tool[] = configuration.getTools();
			IOption[] opts = tool[0].getOptions();
			try {
				for (IOption option : opts) {
					if(option.getValueType() == IOption.INCLUDE_PATH) {
						String[] paths = option.getIncludePaths();
						String[] newPath = new String[paths.length + 1];
						System.arraycopy(paths, 0, newPath, 0, paths.length);
						newPath[newPath.length -1] = "${workspace_loc:" + cuteFolder.getFullPath().toString() + "}";
						ManagedBuildManager.setOption(configuration, tool[0], option, newPath);
					}
				}
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private IFolder createCuteSourceFolder(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.beginTask(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.operation"), 3); //$NON-NLS-1$
		try {
			String relPath= "cute";
				
			IFolder folder= newProject.getProject().getFolder(relPath);
			ISourceEntry srcEntry = CoreModel.newSourceEntry(folder.getFullPath());
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));			
			}
			ICProject cProject = CoreModel.getDefault().create(newProject);
			IPathEntry[] rawPathEntries = cProject.getRawPathEntries();
			ArrayList<IPathEntry> entries = new ArrayList<IPathEntry>(rawPathEntries.length);
			for (IPathEntry entry : rawPathEntries) {
				if(entry.getEntryKind() != IPathEntry.CDT_SOURCE){ //Remove the Project-Rootfolder
					entries.add(entry);
				}
			}
			entries.add(srcEntry);
			cProject.setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]), new SubProgressMonitor(monitor, 2));
			addCuteFiles(folder, monitor);
			return folder;
		} 
		finally {
			monitor.done();
		}
	}
	
	private void addCuteFiles(IFolder folder, IProgressMonitor monitor) throws CoreException {
		
		Enumeration en = CuteLauncherPlugin.getDefault().getBundle().findEntries("cute", "*.h", false);
		while(en.hasMoreElements()) {
			URL url = (URL)en.nextElement();
			String[] elements = url.getFile().split("/"); 
			String filename = elements[elements.length-1];
			IFile targetFile = folder.getFile(filename);
			try {
				targetFile.create(url.openStream(),IResource.FORCE , new SubProgressMonitor(monitor,1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,CuteLauncherPlugin.PLUGIN_ID,42,e.getMessage(), e));
			}
		}		
		
	}
	
	private void createTestSourceFolder(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.beginTask(NewFolderWizardMessages.getString("NewSourceFolderWizardPage.operation"), 3); //$NON-NLS-1$
		try {
			String relPath= "src";
				
			IFolder folder= newProject.getProject().getFolder(relPath);
			ISourceEntry srcEntry = CoreModel.newSourceEntry(folder.getFullPath());
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));			
			}
			ICProject cProject = CoreModel.getDefault().create(newProject);
			IPathEntry[] rawPathEntries = cProject.getRawPathEntries();
			ArrayList<IPathEntry> entries = new ArrayList<IPathEntry>(rawPathEntries.length);
			for (IPathEntry entry : rawPathEntries) {
					entries.add(entry);
			}
			entries.add(srcEntry);
			cProject.setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]), new SubProgressMonitor(monitor, 2));
			addTestFiles(folder, monitor);
			
		} 
		finally {
			monitor.done();
		}
	}
	
	private void addTestFiles(IFolder folder, IProgressMonitor monitor) throws CoreException {
		
		Enumeration en = CuteLauncherPlugin.getDefault().getBundle().findEntries("test", "*.cpp", false);
		while(en.hasMoreElements()) {
			URL url = (URL)en.nextElement();
			String[] elements = url.getFile().split("/"); 
			String filename = elements[elements.length-1];
			IFile targetFile = folder.getFile(filename);
			try {
				targetFile.create(url.openStream(),IResource.FORCE , new SubProgressMonitor(monitor,1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,CuteLauncherPlugin.PLUGIN_ID,42,e.getMessage(), e));
			}
		}		
		
	}

}
