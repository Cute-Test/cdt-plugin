/*******************************************************************************
 * Copyright (c) 2008, Industrial Logic, Inc. All Rights Reserved. 
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Industrial Logic, Inc.:  Mike Bria & John Tangney - initial implementation (based on ideas originating from the work of Emanuel Graf)
 ******************************************************************************/
package ch.hsr.ifs.test.framework.event;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.test.framework.TestFrameworkPlugin;

public abstract class ConsoleEventParser {
	protected List<TestEvent> testEvents;

	public abstract String getComprehensiveLinePattern();

	public abstract String getLineQualifier();

	protected abstract void extractTestEventsFor(IRegion reg, String line)
			throws CoreException;

	public List<TestEvent> eventsFrom(IRegion reg, String line) {
		freshTestEventCollection();
		try {
			extractTestEventsFor(reg, line);
		} catch (CoreException e) {
			TestFrameworkPlugin.getDefault().getLog().log(e.getStatus());
			throwLineParsingException(reg, line, e);
		} catch (Exception e) {
			throwLineParsingException(reg, line, e);
		}
		return testEvents;
	}

	protected void freshTestEventCollection() {
		testEvents = new ArrayList<TestEvent>();
	}
	
	protected void throwLineParsingException(IRegion reg, String line,
			Exception e) {
		throw new RuntimeException("Failure parsing console event {<line=" //$NON-NLS-1$
				+ line + ">, <Reg=" + reg //$NON-NLS-1$
				+ ">} into TestEvent.  Check log for more information.", e); //$NON-NLS-1$
	}

	protected Matcher matcherFor(Pattern pattern, String line)
			throws CoreException {
		Matcher m = pattern.matcher(line);
		if (!m.matches()) {
			throw new CoreException(new Status(Status.ERROR,
					TestFrameworkPlugin.PLUGIN_ID, 1, "Pattern don't match", //$NON-NLS-1$
					null));
		}
		return m;
	}

	protected static String regExUnion(String[] fragments) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < fragments.length; i++) {
			if (i > 0)
				buffer.append("|"); //$NON-NLS-1$
			buffer.append(fragments[i]);
		}
		return buffer.toString();
	}

	protected static String escapeForRegex(String string) {
		return string.replace("]", "\\]").replace("[", "\\[");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
	}
}