/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class GcnoFileVisitor implements IResourceVisitor {

    private final IPath executableLocation;

    public GcnoFileVisitor(IPath executableLocation) {
        this.executableLocation = executableLocation;
    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            if (file.getName().endsWith(".gcno")) {
                handleGcnoFile(file);
            }
        }
        return true;
    }

    private void handleGcnoFile(IFile file) throws CoreException {
        final IPath location = file.getLocation();
        final IPath directory = location.removeLastSegments(1);
        final File workingDirectory = new File(directory.toPortableString());
        if (workingDirectory.isDirectory()) {
            IProject project = file.getProject();
            GcovRunner.runGcov(file.getName(), workingDirectory, project);
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            project.accept(resource -> {
                if (resource instanceof IFile) {
                    IFile memberFile = (IFile) resource;
                    if ("gcov".equals(memberFile.getFileExtension())) {
                        GcovFileParser gcovFileParser = new GcovFileParser(memberFile, executableLocation);
                        gcovFileParser.parse();
                        memberFile.delete(false, new NullProgressMonitor());
                    }
                }
                return true;
            });
        }
    }
}
