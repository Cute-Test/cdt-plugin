/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import static ch.hsr.ifs.cute.gcov.util.StreamUtil.tryClose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.model.Branch;
import ch.hsr.ifs.cute.gcov.model.CoverageModel;
import ch.hsr.ifs.cute.gcov.model.CoverageStatus;
import ch.hsr.ifs.cute.gcov.model.File;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class GcovFileParser {

    private static final Pattern LINE_PATTERN     = Pattern.compile("(.*)(\\d+|-|#####):(\\s*)(\\d+):(.*)$");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "(function )(\\w*)( called )(\\d+)( returned \\d+\\% blocks executed )(\\d+)(\\%)(.*)$");
    private static final Pattern BRANCH_PATTERN   = Pattern.compile("(branch\\s+)(\\d+)(\\s+taken\\s+)(\\d+)(\\%)(.*)$");
    private static final Pattern SOURCE_PATTERN   = Pattern.compile("(.*)(\\d+|-):Source:(.*)$");

    private File     file;
    private IFile    gcovFile;
    private Function currentFunction;
    private Line     currentLine;
    private IPath    executableLocation;

    public GcovFileParser(IFile gcovFile, IPath executableLocation) {
        this.gcovFile = gcovFile;
        this.executableLocation = executableLocation;
    }

    public void parse() throws CoreException {
        InputStream gcovFileContents = gcovFile.getContents();
        BufferedReader in = new BufferedReader(new InputStreamReader(gcovFileContents));
        parse(in);
    }

    private void parse(BufferedReader in) {
        String line;
        try {
            try {
                line = in.readLine();
                if (line != null) {
                    Matcher sourceMatcher = SOURCE_PATTERN.matcher(line);
                    if (sourceMatcher.matches()) {
                        handleSource(sourceMatcher);
                    }
                    if (file == null) {
                        return;
                    }
                    parseContent(in);
                }
            } catch (NumberFormatException e) {
                GcovPlugin.log(e);
            } catch (IOException e) {
                GcovPlugin.log(e);
            }
        } finally {
            tryClose(in);
        }
    }

    private void parseContent(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
            if (functionMatcher.matches()) {
                handleFunction(functionMatcher);
                continue;
            }
            Matcher lineMatcher = LINE_PATTERN.matcher(line);
            if (lineMatcher.matches()) {
                handleLine(lineMatcher);
                continue;
            }
            Matcher branchMatcher = BRANCH_PATTERN.matcher(line);
            if (branchMatcher.matches() && !line.endsWith("taken 0% (throw)")) {
                int taken = Integer.parseInt(branchMatcher.group(4));
                currentLine.addBranch(new Branch(taken));
                continue;
            }
        }
    }

    private void handleSource(Matcher sourceMatcher) {
        String fileName = sourceMatcher.group(3);
        IWorkspace workspace = CCorePlugin.getWorkspace();
        IPath path = new Path(fileName);
        if (!path.isAbsolute()) {
            path = executableLocation.append(path);
        }
        IResource gcovTarget = workspace.getRoot().getFileForLocation(path);
        if (gcovTarget instanceof IFile) {
            IFile gcovTargetFile = (IFile) gcovTarget;
            if (gcovTargetFile.exists()) {
                CoverageModel coverageModel = GcovPlugin.getDefault().getcModel();
                File modelFile = coverageModel.getModelForFile(gcovTargetFile);
                if (modelFile == null) {
                    file = coverageModel.addFileToModel(gcovTargetFile);
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
        } catch (NumberFormatException e) {}
    }

    protected void handleLine(Matcher lineMatcher) {
        try {
            String count = lineMatcher.group(2);
            int lineNumber = Integer.parseInt(lineMatcher.group(4));
            if (count.equalsIgnoreCase("#####")) {
                currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
                currentFunction.addLine(currentLine);
            } else if (count.equalsIgnoreCase("-")) {

            } else {
                int i = Integer.parseInt(count);
                if (currentFunction == null) {
                    currentFunction = new Function("unknown function", 0, 0);
                    file.addFunction(currentFunction);
                }
                if (i > 0) {
                    currentLine = new Line(lineNumber, CoverageStatus.Covered);
                    currentFunction.addLine(currentLine);
                } else {
                    currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
                    currentFunction.addLine(currentLine);
                }
            }
        } catch (NumberFormatException e) {}
    }
}
