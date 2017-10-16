/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
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

	private final BoostWizardAddition addition;

	public BoostHandler(BoostWizardAddition boostWizardAddition) {
		this.addition = boostWizardAddition;
	}

	@Override
	public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
		SubMonitor mon = SubMonitor.convert(pm, 2);
		if (addition.copyBoost) {
			mon.beginTask(Messages.BoostHandler_beginTaskFolders, 2);
			IFolder boostSrcFolder = ProjectTools.createFolder(project, "boost", false);
			IFolder boostFolder = ProjectTools.createFolder(project, "boost/boost", false);
			List<URL> urls = getBoostFiles("boost");
			copyFilesToFolder(boostFolder, new NullProgressMonitor(), urls);
			ProjectTools.setIncludePaths(boostSrcFolder.getFullPath(), project, this);
		}
		mon.done();
	}

	@SuppressWarnings("rawtypes")
	private List<URL> getBoostFiles(String folder) {
		Enumeration en = Activator.getDefault().getBundle().findEntries(folder, "*", false);
		List<URL> list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add((URL) en.nextElement());
		}
		return list;
	}

	private void copyFilesToFolder(IFolder folder, IProgressMonitor monitor, List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String fileName = url.getFile();
			if (fileName.endsWith("/")) {
				IFolder subFolder = ProjectTools.createFolder(folder.getProject(), "boost" + fileName, false);
				copyFilesToFolder(subFolder, mon, getBoostFiles(fileName));
			} else {
				String[] elements = fileName.split("/");
				String filename = elements[elements.length - 1];
				mon.subTask(Messages.BoostHandler_copy + filename);
				IFile targetFile = folder.getFile(filename);
				try {
					targetFile.create(url.openStream(), IResource.FORCE, SubMonitor.convert(monitor, 1));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 42, e.getMessage(), e));
				}
				mon.worked(1);
				mon.done();
			}
		}
	}

	@Override
	public void configureLibProject(IProject project) throws CoreException {
		// Do nothing
	}

	@Override
	public GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.INCLUDE_PATH:
			return new IncludePathStrategy();

		default:
			throw new IllegalArgumentException("Illegal Argument: " + optionType);
		}
	}

}
