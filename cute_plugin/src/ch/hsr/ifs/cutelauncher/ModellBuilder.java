/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule f�r Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.cutelauncher.model.CuteModel;
import ch.hsr.ifs.cutelauncher.model.TestCase;
import ch.hsr.ifs.cutelauncher.model.TestStatus;
import ch.hsr.ifs.cutelauncher.model.TestSuite;

/**
 * @author egraf
 *
 */
public class ModellBuilder implements TestEventHandler {
	
	private CuteModel model = CuteLauncherPlugin.getModel();
	private IPath rtPath;
	private TestCase currentTestCase;
	
	
	
	public ModellBuilder(IPath exePath) {
		super();
		this.rtPath = exePath.removeLastSegments(1);;
	}

	public void handleError(IRegion reg, String[] parts) {
		model.endCurrentTestCase(null, -1, parts[1], TestStatus.error, currentTestCase);
	}

	public void handleSuccess(IRegion reg, String[] parts) {
		model.endCurrentTestCase(null, -1, parts[2], TestStatus.success, currentTestCase);	
	}

	public void handleEnding(IRegion reg, String[] parts) {
		model.endSuite();		
	}

	public void handleBeginning(IRegion reg, String[] parts) {
		model.startNewRun(new TestSuite(parts[1], Integer.parseInt(parts[2]), TestStatus.running));
		
	}

	public void handleFailure(IRegion reg, String[] parts){
		IPath filePath = rtPath.append(parts[1]);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
		int lineNumber = Integer.parseInt(parts[2]);
		model.endCurrentTestCase(file, lineNumber, parts[3], TestStatus.failure, currentTestCase);
	}

	public void handleTestStart(IRegion reg, String[] parts) {
		currentTestCase = new TestCase(parts[1]);
		model.addTest(currentTestCase);		
	}


}