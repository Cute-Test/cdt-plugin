/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.console.IHyperlink;

/**
 * @since 3.0
 * @author Emanuel Graf IFS
 */
public interface ILinkFactory {

	/**
	 * @since 3.0
	 */
	public abstract IHyperlink createLink(IFile file, int lineNumber, String editorId, int fileLength, int fileOffset);

}