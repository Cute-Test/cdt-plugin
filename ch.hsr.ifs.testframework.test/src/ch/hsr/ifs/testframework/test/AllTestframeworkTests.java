/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.testframework.test.hyperlink.HyperlinkSuite;
import ch.hsr.ifs.testframework.test.modelbuilder.ModelBuilderSuite;
import ch.hsr.ifs.testframework.test.patternlistener.PatternListenerSuite;

/**
 * @author Emanuel Graf IFS
 *
 */
public class AllTestframeworkTests extends TestSuite{
	

	public AllTestframeworkTests() {
		super("Testframework All Core Tests"); //$NON-NLS-1$
		addTest(PatternListenerSuite.suite());
		addTest(ModelBuilderSuite.suite());
		addTest(HyperlinkSuite.suite());
	}

	public static Test suite() {
		return new AllTestframeworkTests();
	}

}

