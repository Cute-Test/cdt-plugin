/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * @since 3.0
 */
object LaunchConfigurationsMessages {

   private const val BUNDLE_NAME = "ch.hsr.ifs.testframework.launch.messages"
   private val RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   operator fun get(key: String) = 
         try {
            RESOURCE_BUNDLE.getString(key);
         } catch (e: MissingResourceException) {
            '!' + key + '!';
         }

}
