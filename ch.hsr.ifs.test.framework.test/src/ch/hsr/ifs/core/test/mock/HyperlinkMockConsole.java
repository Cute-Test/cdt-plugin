/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.core.test.mock;

import java.util.Vector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;

/**
 * @author Emanuel Graf
 *
 */
public class HyperlinkMockConsole extends FileInputTextConsole {
	
	private Vector<HyperlinkLocation> links = new Vector<HyperlinkLocation>();

	public HyperlinkMockConsole(String inputFile) {
		super(inputFile);
	}

	@Override
	public void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException {
		super.addHyperlink(hyperlink, offset, length);
		links.add(new HyperlinkLocation(hyperlink, offset, length));
	}

	public Vector<HyperlinkLocation> getLinks() {
		return links;
	}
	
	

}
