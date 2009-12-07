package org.ginkgo.gcov.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class LineCoverageParser implements IParser {

	public static final String UNCOVER_MARKER_TYPE = "org.ginkgo.gcov.lineUnCoverMarker";
	public static final String COVER_MARKER_TYPE = "org.ginkgo.gcov.lineCoverMarker";
	public static final String COVERAGE_MARKER_TYPE = "org.ginkgo.gcov.CoverageMarker";

	private IFile sourceFile = null;

	private class MyResourceVisitor implements IResourceVisitor {
		private String sourceFileName;

		private MyResourceVisitor(String FileName) {
			sourceFileName = FileName;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource.getName().equals(sourceFileName)) {
				sourceFile = (IFile) resource;
				return false;
			} else {
				return true;
			}
		}

	}

	public void parse(IFile file) {
		String lineNum = null;
		String execCount = null;
		String line = null;
		sourceFile = null;

		IProject project = file.getProject();
		String sourceFileName = file.getName().replaceAll("\\.gcov", "");
		try {
			project.accept(new MyResourceVisitor(sourceFileName));
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		System.out.println(sourceFile);
		if (sourceFile == null) {
			return;
		}
		deleteMarkers(sourceFile);
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(file
					.getContents()));
			while ((line = in.readLine()) != null) {
				String[] s = line.split(":");
				if (s.length == 3) {
					execCount = s[0].trim();
					lineNum = s[1].trim();
					if (execCount.equals("#####")) {
						Map attributes = new HashMap();
						MarkerUtilities.setMessage(attributes, "Not Covered");
						MarkerUtilities.setLineNumber(attributes, Integer
								.parseInt(lineNum));
						MarkerUtilities.createMarker(sourceFile, attributes,
								UNCOVER_MARKER_TYPE);
					} else if (execCount.equals("-")) {

					} else {
						Map attributes = new HashMap();
						MarkerUtilities.setMessage(attributes, execCount);
						MarkerUtilities.setLineNumber(attributes, Integer
								.parseInt(lineNum));
						MarkerUtilities.createMarker(sourceFile, attributes,
								COVER_MARKER_TYPE);
					}
					continue;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

}
