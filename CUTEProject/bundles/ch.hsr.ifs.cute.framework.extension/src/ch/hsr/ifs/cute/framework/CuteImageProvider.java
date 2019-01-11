/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.framework;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;

import ch.hsr.ifs.testframework.ImageProvider;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;


/**
 * @author egraf
 * @since 3.1
 *
 */
public class CuteImageProvider extends ImageProvider {

    protected Map<Integer, String> pathMap = new TreeMap<>();

    public CuteImageProvider() {
        pathMap.put(ImageProvider.Companion.getAPP_LOGO(), "obj16/cute_app.png");
    }

    @Override
    public ImageDescriptor getImage(int key) {
        return TestFrameworkPlugin.Companion.getImageDescriptor(getPath(key));
    }

    protected String getPath(int key) {
        return pathMap.get(key);
    }
}
