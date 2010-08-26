/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.core.test.mock;

import org.eclipse.ui.console.IHyperlink;

/**
 * @author Emanuel Graf IFS
 *
 */
public class HyperlinkLocation {

	private IHyperlink link;
	private int offset;
	private int length;

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
