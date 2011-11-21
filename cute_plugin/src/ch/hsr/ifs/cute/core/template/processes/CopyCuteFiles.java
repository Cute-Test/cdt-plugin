/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.core.template.processes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * @author Emanuel Graf
 *
 */
public class CopyCuteFiles extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args,
			String processId, IProgressMonitor monitor)
	throws ProcessFailureException {

		String projectName = args[0].getSimpleValue();
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String sourceDir = args[1].getSimpleValue();
		String targetDir = args[2].getSimpleValue();

		URL path;
		try {
			path = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, sourceDir);
			if (path == null) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, "Copy CUTE files failure: template source not found:" + sourceDir)); //$NON-NLS-1$
			}
		} catch (IOException e1) {
			throw new ProcessFailureException("Copy CUTE files failure: template source not found: " + sourceDir); //$NON-NLS-1$
		}

		File[] filenames = getFiles(path);

		for (File file : filenames) {
			
			InputStream contents = null;
			try {
				contents = new FileInputStream(file);
				copyFile(projectHandle, targetDir, file, contents);
				projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (IOException e) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, "Could not open File: " + file.getAbsolutePath())); //$NON-NLS-1$
			} catch (CoreException e) {
				throw new ProcessFailureException("Could not write File: " + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		
	}

	private void copyFile(IProject projectHandle, String targetDir, File file,
			InputStream contents) throws CoreException {
		IFile iFile = projectHandle.getFile(targetDir + "/" + file.getName()); //$NON-NLS-1$
		if (!iFile.getParent().exists()) {
			ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
		}

		if (iFile.exists()) {
				iFile.setContents(contents, true, true, null);
		} else {
			iFile.create(contents, true, null);
			iFile.refreshLocal(IResource.DEPTH_ONE, null);
		}
	}

	private File[] getFiles(URL path) throws ProcessFailureException {
		File file = new File(path.getFile());
		if(file.isDirectory()) {
			return file.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.endsWith(".h"); //$NON-NLS-1$
				}});
		}else {
			throw new ProcessFailureException("Source is not a Direcotry"); //$NON-NLS-1$
		}
	}

}
