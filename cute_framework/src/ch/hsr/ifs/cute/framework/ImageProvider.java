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
package ch.hsr.ifs.cute.framework;


import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author egraf
 *
 */
public abstract class ImageProvider {
	
	public static final int APP_LOGO = 0;
	abstract public ImageDescriptor getImage(int key);

}
