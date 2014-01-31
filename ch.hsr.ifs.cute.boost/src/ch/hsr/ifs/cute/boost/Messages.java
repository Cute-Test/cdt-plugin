/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.boost;

import org.eclipse.osgi.util.NLS;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.boost.messages";
	public static String BoostHandler_beginTaskFolders;
	public static String BoostHandler_copy;
	public static String BoostWizardAddition_0;
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
