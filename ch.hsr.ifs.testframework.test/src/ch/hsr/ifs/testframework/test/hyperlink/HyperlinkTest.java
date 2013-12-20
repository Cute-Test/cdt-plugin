/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.hyperlink;

import java.util.Vector;

import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.testframework.event.TestEventHandler;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.test.ConsoleTest;
import ch.hsr.ifs.testframework.test.mock.FileInputTextConsole;
import ch.hsr.ifs.testframework.test.mock.HyperlinkLocation;
import ch.hsr.ifs.testframework.test.mock.HyperlinkMockConsole;
import ch.hsr.ifs.testframework.test.mock.MockLinkFactory;
import ch.hsr.ifs.testframework.ui.ConsoleLinkHandler;

/**
 * @author Emanuel Graf
 * 
 */
public class HyperlinkTest extends ConsoleTest {

	private TestEventHandler consoleLinkHandler;
	private int expectedLinkOffset;
	private int expectedLinkLength;

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		consoleLinkHandler = new ConsoleLinkHandler(new Path(""), tc, new MockLinkFactory());
		lis.addHandler(consoleLinkHandler);
	}

	@Override
	protected FileInputTextConsole getConsole() {
		return new HyperlinkMockConsole(filePathRoot + getInputFilePath());
	}

	public void testLinks() throws Exception {
		emulateTestRun();
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
		String[] linkDimensions = firstConsoleLine().split(",");
		expectedLinkOffset = Integer.parseInt(linkDimensions[0]);
		expectedLinkLength = Integer.parseInt(linkDimensions[1]);
	}

	@Override
	protected String getInputFilePath() {
		return "hyperlinkTests/linkTest.txt";
	}

}
