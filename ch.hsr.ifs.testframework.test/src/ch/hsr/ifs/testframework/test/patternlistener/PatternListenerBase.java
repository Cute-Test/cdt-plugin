/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.patternlistener;

import ch.hsr.ifs.testframework.test.ConsoleTest;

/**
 * @author Emanuel Graf IFS
 * 
 */
public abstract class PatternListenerBase extends ConsoleTest {
	@Override
	protected String getInputFilePath() {
		return "patternListenerTests/" + getInputFileName();
	}

	protected abstract String getInputFileName();
}