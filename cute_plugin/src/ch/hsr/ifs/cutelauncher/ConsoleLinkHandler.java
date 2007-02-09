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
package ch.hsr.ifs.cutelauncher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;

/**
 * @author egraf
 *
 */
public class ConsoleLinkHandler implements TestEventHandler {
	
	private TextConsole console; 
	private IPath rtPath;
	
	

	public ConsoleLinkHandler(IPath exePath, TextConsole console) {
		super();
		rtPath = exePath.removeLastSegments(1);
		this.console = console;
	}


	public void handleBeginning(IRegion reg, String[] parts) {
	}


	public void handleEnding(IRegion reg, String[] parts) {
	}


	public void handleError(IRegion reg, String[] parts) {
		
	}

	public void handleSuccess(IRegion reg, String[] parts) {
	}

	public void handleFailure(IRegion reg, String[] parts) {
		
		try {
			IPath filePath = rtPath.append(parts[1]);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			int lineNumber = Integer.parseInt(parts[2]);
			IHyperlink link = new FileLink(file, null,-1,-1,lineNumber);
			console.addHyperlink(link, reg.getOffset(), reg.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void handleTestStart(IRegion reg, String[] parts) {
	}


}
