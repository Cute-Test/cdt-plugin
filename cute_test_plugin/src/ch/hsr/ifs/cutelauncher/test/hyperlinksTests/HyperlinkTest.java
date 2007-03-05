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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.IHyperlink;

import ch.hsr.ifs.cutelauncher.ConsoleLinkHandler;
import ch.hsr.ifs.cutelauncher.CutePatternListener;
import ch.hsr.ifs.cutelauncher.test.ConsoleTest;

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

	public void testLinks() {
		IHyperlink[] links = tc.getHyperlinks();
		try {
			Job.getJobManager().join(tc, new NullProgressMonitor());
		} catch (OperationCanceledException e) {
		} catch (InterruptedException e) {
		}
		assertEquals(1, links.length);
	}

}
