/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.framework.ui;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.ImageProvider;

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
		return CuteFrameworkPlugin.getImageDescriptor(getPath(key));
	}

	protected String getPath(int key) {
		return pathMap.get(key);
	}

}
