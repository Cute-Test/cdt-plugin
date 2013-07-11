/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.event.TestEventHandler;

/**
 * @author egraf
 *
 */
public class ModellBuilder extends TestEventHandler {

	private final Model model = TestFrameworkPlugin.getModel();
	private final IPath rtPath;
	private TestCase lastTestCase;
	private TestCase currentTestCase;
	private final ILaunch launch;

	public ModellBuilder(IPath exePath, ILaunch launch) {
		super();
		this.rtPath = exePath;
		this.launch = launch;
	}
	public ModellBuilder(IPath path) {
		this(path, null);
	}

	@Override
	public void handleError(IRegion reg, String testName, String msg) {
		if(currentTestCase != null) {
			model.endCurrentTestCase(null, -1, msg, TestStatus.error, currentTestCase);
			endTestCase();
		}else {
			unexpectedTestCaseEnd();
		}
	}
	private void unexpectedTestCaseEnd() {
		if(lastTestCase != null) {
			model.endCurrentTestCase(null, -1, Messages.ModellBuilder_0, TestStatus.error, lastTestCase);
		}

	}
	private void endTestCase() {
		lastTestCase = currentTestCase;
		currentTestCase = null;
	}

	@Override
	public void handleSuccess(IRegion reg, String name, String msg) {
		if(currentTestCase != null) {
			model.endCurrentTestCase(null, -1, msg, TestStatus.success, currentTestCase);
			endTestCase();
		}else {
			unexpectedTestCaseEnd();
		}
	}

	@Override
	public void handleEnding(IRegion reg, String suitename) {
		model.endSuite();
	}

	@Override
	public void handleBeginning(IRegion reg, String suitename, String suitesize) {
		model.startSuite(new TestSuite(suitename, Integer.parseInt(suitesize), TestStatus.running));
	}

	@Override
	public void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason){
		if(currentTestCase != null) {

			IPath filePath = getWorkspaceFile(fileName, rtPath);		
			
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			if(file == null){
				try {
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new URI("file:" + filePath.toPortableString()), IResource.FILE);
					if(files.length > 0)
					{
						file = files[0];
					}
				} catch (URISyntaxException e) {
				}
			}
			int lineNumber = Integer.parseInt(lineNo);
			model.endCurrentTestCase(file, lineNumber, reason, TestStatus.failure, currentTestCase);
			endTestCase();
		} else {
			unexpectedTestCaseEnd();
		}

	}

	@Override
	public void handleTestStart(IRegion reg, String suitename) {
		currentTestCase = new TestCase(suitename);
		model.addTest(currentTestCase);
	}

	@Override
	public void handleSessionEnd() {
		model.endSession(currentTestCase);
	}

	@Override
	public void handleSessionStart() {
		model.startSession(launch);
	}

}
