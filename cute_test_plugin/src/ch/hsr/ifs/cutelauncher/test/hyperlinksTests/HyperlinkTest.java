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
package ch.hsr.ifs.cutelauncher.test.hyperlinksTests;

import java.util.Vector;

import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cutelauncher.ConsoleLinkHandler;
import ch.hsr.ifs.cutelauncher.CutePatternListener;
import ch.hsr.ifs.cutelauncher.test.ConsoleTest;
import ch.hsr.ifs.cutelauncher.test.internal.console.FileInputTextConsole;

/**
 * @author Emanuel Graf
 *
 */
public class HyperlinkTest extends ConsoleTest {

	private ConsoleLinkHandler consoleLinkHandler;

	@Override
	protected void addTestEventHandler(CutePatternListener lis) {
		consoleLinkHandler = new ConsoleLinkHandler(new Path(""), tc);
		lis.addHandler(consoleLinkHandler);
	}

	@Override
	protected String getInputFile() {
		return "testDefs/hyperlinkTests/linkTest.txt";
	}
	
	

	@Override
	protected FileInputTextConsole getConsole() {
		return new HyperlinkTestConsole(getInputFile());
	}

	public void testLinks() {
		if (tc instanceof HyperlinkTestConsole) {
			HyperlinkTestConsole linkConsole = (HyperlinkTestConsole) tc;
			Vector<TestHyperlinks> links = linkConsole.getLinks();
			assertEquals(1, links.size());
			TestHyperlinks link = links.firstElement();
			assertEquals(137, link.getOffset());
			assertEquals(76, link.getLength());
		}
		
	}

}
