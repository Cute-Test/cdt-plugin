/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf & Guido Zgraggen- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.core.test.mock;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.console.IHyperlink;

/**
 * @author Emanuel Graf
 *
 */
public class HyperlinkMock implements IHyperlink{

	private int offset;
	private IFile file;
	private String editorId;
	private int fileLength;
	private int lineNmber;


	public HyperlinkMock(IFile file, int lineNumber, String editorId, int fileLength, int fileOffset) {
		super();
		this.file = file;
		this.lineNmber = lineNumber;
		this.editorId = editorId;
		this.fileLength = fileLength;
		this.offset = fileOffset;
	}

	public int getOffset() {
		return offset;
	}

	public IFile getFile() {
		return file;
	}

	public String getEditorId() {
		return editorId;
	}

	public int getFileLength() {
		return fileLength;
	}

	public int getLineNmber() {
		return lineNmber;
	}

	public void linkEntered() {
	}

	public void linkExited() {
	}

	public void linkActivated() {
	}
	
	

}
