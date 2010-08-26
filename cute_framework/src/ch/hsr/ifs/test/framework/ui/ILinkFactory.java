/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.test.framework.ui;

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