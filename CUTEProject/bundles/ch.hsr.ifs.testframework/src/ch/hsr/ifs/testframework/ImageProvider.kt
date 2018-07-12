/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework

import org.eclipse.jface.resource.ImageDescriptor

/**
 * @author egraf
 *
 */
abstract class ImageProvider {
	abstract fun getImage(key: Int): ImageDescriptor?

	companion object {
		val APP_LOGO = 0
	}
}