/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;
import ch.hsr.ifs.cute.gcov.util.ProjectUtil;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class GcovUpdateJob extends Job {
	private final IProject project;

	GcovUpdateJob(String name, IProject project) {
		super(name);
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (project.hasNature(GcovNature.NATURE_ID)) {
				Collection<ch.hsr.ifs.cute.gcov.model.File> files = GcovPlugin.getDefault().getcModel().getMarkedFiles();
				for (ch.hsr.ifs.cute.gcov.model.File file : files) {
					ProjectUtil.deleteMarkers(file.getFile());
				}
				GcovPlugin.getDefault().getcModel().clearModel();
				updateGcov(project);
				for (IProject refProj : project.getReferencedProjects()) {
					updateGcov(refProj);
				}
				files = GcovPlugin.getDefault().getcModel().getMarkedFiles();
				for (ch.hsr.ifs.cute.gcov.model.File file : files) {
					markFile(file);
				}
			}
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, "Error");
		}
		return new Status(IStatus.OK, GcovPlugin.PLUGIN_ID, "OK");
	}

	private void markFile(ch.hsr.ifs.cute.gcov.model.File file) throws CoreException {
		IFile cppFile = file.getFile();
		for (Function f : file.getFunctions()) {
			for (Line l : f.getLines()) {
				switch (l.getStatus()) {
				case Covered:
					createMarker(cppFile, l.getNr(), "covered", GcovPlugin.COVER_MARKER_TYPE);
					break;
				case PartiallyCovered:
					createMarker(cppFile, l.getNr(), "partially covered", GcovPlugin.PARTIALLY_MARKER_TYPE);
					break;
				case Uncovered:
					createMarker(cppFile, l.getNr(), "uncovered", GcovPlugin.UNCOVER_MARKER_TYPE);
					break;
				default:
					break;
				}
			}
		}
	}

	private void updateGcov(IProject iProject) {
		ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(iProject);
		IPath outputDirectory = desc.getActiveConfiguration().getBuildSetting().getOutputDirectories()[0].getLocation();
		try {
			iProject.accept(new GcnoFileVisitor(outputDirectory));
		} catch (CoreException e) {
			GcovPlugin.log(e);
		}
	}

	private void createMarker(IFile cppFile, int lineNum, String message, String type) throws CoreException {
		Map<String, String> attributes = new HashMap<String, String>();
		MarkerUtilities.setMessage(attributes, message);
		MarkerUtilities.setLineNumber(attributes, lineNum);
		MarkerUtilities.createMarker(cppFile, attributes, type);
	}
}