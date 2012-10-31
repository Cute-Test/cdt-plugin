/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;

import ch.hsr.ifs.testframework.ImageProvider;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;

/**
 * @author egraf
 *
 */
public class FallbackImageProvider extends ImageProvider {

	protected Map<Integer, String> pathMap = new TreeMap<Integer, String>();

	public FallbackImageProvider() {
		pathMap.put(APP_LOGO, "obj16/empty_app.gif"); //$NON-NLS-1$
	}
	
	@Override
	public ImageDescriptor getImage(int key) {
		return TestFrameworkPlugin.getImageDescriptor(getPath(key));
	}

	protected String getPath(int key) {
		return pathMap.get(key);
	}

}
