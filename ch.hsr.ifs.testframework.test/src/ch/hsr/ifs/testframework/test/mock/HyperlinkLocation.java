/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.mock;

import org.eclipse.ui.console.IHyperlink;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class HyperlinkLocation {

	private final IHyperlink link;
	private final int offset;
	private final int length;

	public HyperlinkLocation(IHyperlink hyperlink, int offset, int length) {
		this.link = hyperlink;
		this.offset = offset;
		this.length = length;
	}

	public IHyperlink getLink() {
		return link;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

}
