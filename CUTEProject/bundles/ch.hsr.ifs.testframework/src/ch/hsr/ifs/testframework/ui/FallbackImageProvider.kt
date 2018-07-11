/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import java.util.TreeMap
import org.eclipse.jface.resource.ImageDescriptor
import ch.hsr.ifs.testframework.ImageProvider
import ch.hsr.ifs.testframework.TestFrameworkPlugin

/**
 * @author egraf
 *
 */
class FallbackImageProvider : ImageProvider() {
   private val pathMap = mapOf(
         APP_LOGO to "obj16/empty_app.gif"
   )

   override fun getImage(key: Int): ImageDescriptor? {
      return TestFrameworkPlugin.getImageDescriptor(pathMap[key])
   }
}