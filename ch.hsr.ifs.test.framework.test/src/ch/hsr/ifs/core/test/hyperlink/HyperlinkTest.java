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
package ch.hsr.ifs.core.test.hyperlink;

import java.util.Vector;

import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.core.test.ConsoleTest;
import ch.hsr.ifs.core.test.mock.FileInputTextConsole;
import ch.hsr.ifs.core.test.mock.HyperlinkLocation;
import ch.hsr.ifs.core.test.mock.HyperlinkMockConsole;
import ch.hsr.ifs.core.test.mock.MockLinkFactory;
import ch.hsr.ifs.test.framework.launch.ConsolePatternListener;
import ch.hsr.ifs.test.framework.ui.ConsoleLinkHandler;

/**
 * @author Emanuel Graf
 * 
 */
public class HyperlinkTest extends ConsoleTest {

	private ConsoleLinkHandler consoleLinkHandler;
	private int expectedLinkOffset;
	private int expectedLinkLength;

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		consoleLinkHandler = new ConsoleLinkHandler(new Path(""), tc, new MockLinkFactory()); //$NON-NLS-1$
		lis.addHandler(consoleLinkHandler); 
	}

	@Override
	protected FileInputTextConsole getConsole() {
		return new HyperlinkMockConsole(filePathRoot + getInputFilePath());
	}

	public void testLinks() throws Exception {
		if (tc instanceof HyperlinkMockConsole) {
			HyperlinkMockConsole linkConsole = (HyperlinkMockConsole) tc;
			Vector<HyperlinkLocation> links = linkConsole.getLinks();
			assertEquals(1, links.size());

			grabExpectedLinkDimensions();
			HyperlinkLocation link = links.firstElement();
			assertEquals(expectedLinkOffset, link.getOffset());
			assertEquals(expectedLinkLength, link.getLength());
		}

	}

	private void grabExpectedLinkDimensions() throws Exception {
		String[] linkDimensions = firstConsoleLine().split(","); //$NON-NLS-1$
		expectedLinkOffset = Integer.parseInt(linkDimensions[0]);
		expectedLinkLength = Integer.parseInt(linkDimensions[1]);
	}

	@Override
	protected String getInputFilePath() {
		return "hyperlinkTests/linkTest.txt"; //$NON-NLS-1$
	}

}