/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.mock;

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