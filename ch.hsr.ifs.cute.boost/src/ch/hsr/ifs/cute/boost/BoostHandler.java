/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.boost;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;
import ch.hsr.ifs.cute.ui.IIncludeStrategyProvider;
import ch.hsr.ifs.cute.ui.IncludePathStrategy;
import ch.hsr.ifs.cute.ui.ProjectTools;

/**
 * @author Emanuel Graf IFS
 *
 */
public class BoostHandler implements ICuteWizardAdditionHandler, IIncludeStrategyProvider {

	private BoostWizardAddition addition;

	public BoostHandler(BoostWizardAddition boostWizardAddition) {
		this.addition = boostWizardAddition;
	}

	@Override
	public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
		SubMonitor mon = SubMonitor.convert(pm, 2);
		if(addition.copyBoost) {
			mon.beginTask("Create Boost Folders", 2);
			IFolder boostSrcFolder = ProjectTools.createFolder(project, "boost", true); //$NON-NLS-1$
			IFolder boostFolder = ProjectTools.createFolder(project, "boost/boost", false); //$NON-NLS-1$
			List<URL> urls = getBoostFiles("boost"); //$NON-NLS-1$
			copyFilesToFolder(boostFolder, new NullProgressMonitor(), urls);
			
			ProjectTools.setIncludePaths(boostSrcFolder.getFullPath(), project, this);
		}
		mon.done();
	}
	
	@SuppressWarnings("rawtypes")
	private List<URL> getBoostFiles(String folder) {
		Enumeration en = Activator.getDefault().getBundle().findEntries(folder, "*", false); //$NON-NLS-1$
		List<URL>list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add((URL) en.nextElement());
		}
		return list;
	}

	private void copyFilesToFolder(IFolder folder, IProgressMonitor monitor,
			List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String fileName = url.getFile();
			if(fileName.endsWith("/")){ //$NON-NLS-1$
				IFolder subFolder = ProjectTools.createFolder(folder.getProject(), "boost" + fileName, false); //$NON-NLS-1$
				copyFilesToFolder(subFolder, mon, getBoostFiles(fileName));
			}else {
				String[] elements = fileName.split("/"); //$NON-NLS-1$
				String filename = elements[elements.length-1];
				mon.subTask(Messages.BoostHandler_copy + filename);
				IFile targetFile = folder.getFile(filename);
				try {
					targetFile.create(url.openStream(),IResource.FORCE , new SubProgressMonitor(monitor,1));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,42,e.getMessage(), e));
				}
				mon.worked(1);
				mon.done();
			}
		}
	}

	@Override
	public void configureLibProject(IProject project) throws CoreException {
		//Do nothing
	}

	@Override
	public GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.INCLUDE_PATH:
			return new IncludePathStrategy();

		default:
			throw new IllegalArgumentException("Illegal Argument: "+optionType); //$NON-NLS-1$
		}
	}

}
