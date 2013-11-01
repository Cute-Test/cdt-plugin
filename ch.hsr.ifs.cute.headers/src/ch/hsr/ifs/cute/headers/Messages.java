/*******************************************************************************
 * Copyright (c) 2007-2013, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.headers.messages"; //$NON-NLS-1$
	public static String CuteHeaders_copy;
	public static String CuteHeaders_copySuite;
	public static String CuteHeaders_copyTestCPP;
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
