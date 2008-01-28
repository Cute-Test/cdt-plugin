/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.text.IRegion;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.TestEventHandler;

/**
 * @author egraf
 *
 */
public class ModellBuilder extends TestEventHandler {
	
	private CuteModel model = CuteLauncherPlugin.getModel();
	private IPath rtPath;
	private TestCase currentTestCase;
	private ILaunch launch;
	private ILaunchConfiguration config;
	
	public ModellBuilder(IPath exePath, ILaunch launch,ILaunchConfiguration config) {
		super();
		this.rtPath = exePath.removeLastSegments(1);
		this.launch = launch; 
		this.config=config;
	}
	public ModellBuilder(IPath exePath, ILaunch launch) {
		this(exePath,launch,null);
	}
	public ModellBuilder(IPath path) {
		this(path, null);
	}

	public void handleError(IRegion reg, String testName, String msg) {
		model.endCurrentTestCase(null, -1, msg, TestStatus.error, currentTestCase);
	}

	public void handleSuccess(IRegion reg, String name, String msg) {
		model.endCurrentTestCase(null, -1, msg, TestStatus.success, currentTestCase);	
	}

	public void handleEnding(IRegion reg, String suitename) {
		model.endSuite();
	}

	public void handleBeginning(IRegion reg, String suitename, String suitesize) {
		model.startSuite(new TestSuite(suitename, Integer.parseInt(suitesize), TestStatus.running));
	}

	public void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason){
		IPath filePath=null;
		
		if(config==null)filePath = rtPath.append(fileName);
		else{
			try{
				if(false==config.getAttribute("useCustomSrcPath", false))filePath = rtPath.append(fileName);	
				else{
					String rootpath=org.eclipse.core.runtime.Platform.getLocation().toOSString();
					String customSrcPath=config.getAttribute("customSrcPath","");
					String fileSeparator=System.getProperty("file.separator");
					filePath=new org.eclipse.core.runtime.Path(rootpath+customSrcPath+fileSeparator+fileName);
				}
			}catch(CoreException ce){CuteLauncherPlugin.getDefault().getLog().log(ce.getStatus());}
		}
		//String rootpath=ResourcesPlugin.getWorkspace().getRoot().getFullPath().toOSString();
		//the above does not return the location in the OS  
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
		int lineNumber = Integer.parseInt(lineNo);
		model.endCurrentTestCase(file, lineNumber, reason, TestStatus.failure, currentTestCase);
		/*fileName:MakeTest.cpp
		filePath:D:/runtime-EclipseApplication/sourcePathTestingPrj/src/MakeTest.cpp
		rtpath:D:/runtime-EclipseApplication/sourcePathTestingPrj/src/bin
		file:L/sourcePathTestingPrj/src/MakeTest.cpp	*/
	}

	public void handleTestStart(IRegion reg, String suitename) {
		currentTestCase = new TestCase(suitename);
		model.addTest(currentTestCase);		
	}

	@Override
	public void handleSessionEnd() {
		model.endSession();
	}

	@Override
	public void handleSessionStart() {
		model.startSession(launch);
	}


}
