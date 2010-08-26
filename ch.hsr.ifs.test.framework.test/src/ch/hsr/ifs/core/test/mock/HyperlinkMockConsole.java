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
