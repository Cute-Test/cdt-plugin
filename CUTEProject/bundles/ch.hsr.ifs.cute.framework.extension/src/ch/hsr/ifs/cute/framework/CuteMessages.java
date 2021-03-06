/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.framework;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ch.hsr.ifs.testframework.Messages;


/**
 * @since 3.1
 */
public class CuteMessages implements Messages {

    private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.framework.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public CuteMessages() {}

    @Override
    public String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
