/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.core.resources.IFile
import org.eclipse.debug.ui.console.FileLink

/**
 * @author Emanuel Graf IFS
 * @since 3.0
 *
 */
class ConsoleLinkFactory : ILinkFactory {
   override fun createLink(file: IFile?, lineNumber: Int, editorId: String?, fileLength: Int, fileOffset: Int): FileLink? {
      return FileLink(file, editorId, fileOffset, fileLength, lineNumber)
   }
}
