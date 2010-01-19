package org.ginkgo.gcov.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class LineMarkerCoverageParser extends LineCoverageParser implements IParser {

	public static final String COVERAGE_MARKER_TYPE = "org.ginkgo.gcov.CoverageMarker";

	@Override
	protected void parse(IFile cppFile, IFile gcovFile) throws CoreException, IOException {
		String lineNum;
		String execCount;
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(gcovFile
				.getContents()));
		while ((line = in.readLine()) != null) {
			String[] s = line.split(":");
			if (s.length == 3) {
				execCount = s[0].trim();
				lineNum = s[1].trim();
				if (execCount.equals("#####")) {
					notCovered(cppFile, lineNum);
				} else if (execCount.equals("-")) {

				} else {
					covered(cppFile, lineNum, execCount);
				}
				continue;
			}
		}
	}

	protected void covered(IFile cppFile, String lineNum, String execCount) throws CoreException {
		createMarker(cppFile, Integer.parseInt(lineNum), execCount, COVER_MARKER_TYPE);
	}

	protected void notCovered(IFile cppFile, String lineNum) throws CoreException {
		createMarker(cppFile, Integer.parseInt(lineNum), "Not Covered", UNCOVER_MARKER_TYPE);
	}

}
