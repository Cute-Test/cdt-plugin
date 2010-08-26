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
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;

import ch.hsr.ifs.test.framework.event.TestEventHandler;

/**
 * @author Emanuel Graf (IFS)
 *
 */
public class ConsoleLinkHandler extends TestEventHandler{
	
	private TextConsole console; 
	private IPath rtPath;
	private ILinkFactory linkFactory;

	public ConsoleLinkHandler(IPath exePath, TextConsole console) {
		this(exePath, console, new ConsoleLinkFactory());
	}

	/**
	 * @since 3.0
	 */
	public ConsoleLinkHandler(IPath rtPath, TextConsole console, ILinkFactory linkFactory) {
		super();
		this.console = console;
		this.rtPath = rtPath;
		this.linkFactory = linkFactory;
	}




	@Override
	public void handleBeginning(IRegion reg, String suitename, String suitesize) {
	}


	@Override
	public void handleEnding(IRegion reg, String suitename) {
	}


	@Override
	public void handleError(IRegion reg, String testName, String msg) {
		
	}

	@Override
	public void handleSuccess(IRegion reg, String name, String msg) {
	}

	@Override
	public void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
		IPath filePath=rtPath.append(fileName);
		try {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			int lineNumber = Integer.parseInt(lineNo);
			IHyperlink link = linkFactory.createLink(file, lineNumber, null, -1, -1);
			console.addHyperlink(link, reg.getOffset(), reg.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleTestStart(IRegion reg, String suitename) {
	}


	@Override
	public void handleSessionEnd() {
	}


	@Override
	public void handleSessionStart() {
	}

}
