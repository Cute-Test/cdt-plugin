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
package ch.hsr.ifs.cute.gcov.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.model.Branch;
import ch.hsr.ifs.cute.gcov.model.CoverageStatus;
import ch.hsr.ifs.cute.gcov.model.File;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ModelBuilderLineParser extends LineCoverageParser {
	
	private static final Pattern LINE_PATTERN = Pattern.compile("(.*)(\\d+|-|#####):(\\s*)(\\d+):(.*)$"); //$NON-NLS-1$
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("(function )(\\w*)( called )(\\d+)( returned \\d+\\% blocks executed )(\\d+)(\\%)(.*)$"); //$NON-NLS-1$
	private static final Pattern BRANCH_PATTERN = Pattern.compile("(branch\\s+)(\\d+)(\\s+taken\\s+)(\\d+)(\\%)(.*)$"); //$NON-NLS-1$
	
	File file;
	Function currentFunction;
	Line currentLine;

	@Override
	public void parse(IFile cppFile, Reader gcovFile) throws CoreException, IOException {
		file = GcovPlugin.getDefault().getcModel().addFileToModel(cppFile);
		BufferedReader in = new BufferedReader(gcovFile);
		String line;
		while ((line = in.readLine()) != null) {
			Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
			if(functionMatcher.matches()) {
				handleFunction(functionMatcher);
				continue;
			}
			Matcher lineMatcher = LINE_PATTERN.matcher(line);
			if(lineMatcher.matches()) {
				handleLine(lineMatcher);
				continue;
			}
			Matcher branchMatcher = BRANCH_PATTERN.matcher(line);
			if(branchMatcher.matches()) {
				int taken = Integer.parseInt(branchMatcher.group(4));
				currentLine.addBranch(new Branch(taken));				
			}

		}
		for (Function f : file.getFunctions()) {
			for (Line l : f.getLines()) {
				switch (l.getStatus()) {
				case Covered:
					createMarker(cppFile, l.getNr(), "covered", GcovPlugin.COVER_MARKER_TYPE); //$NON-NLS-1$
					break;
				case PartiallyCovered:
					createMarker(cppFile, l.getNr(), "partially covered", GcovPlugin.PARTIALLY_MARKER_TYPE); //$NON-NLS-1$
					break;
				case Uncovered:
					createMarker(cppFile, l.getNr(), "uncovered", GcovPlugin.UNCOVER_MARKER_TYPE); //$NON-NLS-1$
					break;
				default:
					break;
				}
			}
		}
	}

	protected void handleFunction(Matcher functionMatcher) {
		try {
			String name = functionMatcher.group(2);
			int called = Integer.parseInt(functionMatcher.group(4));
			int execBlocks = Integer.parseInt(functionMatcher.group(6));
			currentFunction = new Function(name, called, execBlocks);
			file.addFunction(currentFunction);
		}catch(NumberFormatException e) {}
	}

	protected void handleLine(Matcher lineMatcher) {
		try {
			String count = lineMatcher.group(2);
			int lineNumber = Integer.parseInt(lineMatcher.group(4));
			if(count.equalsIgnoreCase("#####")) { //$NON-NLS-1$
				currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
				currentFunction.addLine(currentLine);
			}else if(count.equalsIgnoreCase("-")) { //$NON-NLS-1$

			}else{
				int i = Integer.parseInt(count);
				if(i > 0) {
					currentLine = new Line(lineNumber, CoverageStatus.Covered);
					currentFunction.addLine(currentLine);
				}else {
					currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
					currentFunction.addLine(currentLine);
				}
			}

		}catch (NumberFormatException e) {}
	}
}
