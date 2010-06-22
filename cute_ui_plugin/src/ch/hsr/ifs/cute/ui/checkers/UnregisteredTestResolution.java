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
package ch.hsr.ifs.cute.ui.checkers;

import org.eclipse.cdt.codan.ui.AbstarctCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

/**
 * @author Emanuel Graf IFS
 *
 */
public class UnregisteredTestResolution extends AbstarctCodanCMarkerResolution {

	public UnregisteredTestResolution() {
	}


	public String getLabel() {
		return "Add test to suite";
	}


	@Override
	public void apply(IMarker marker, IDocument document) {
		int pos = getOffset(marker, document);
		IResource res = marker.getResource();
		


	}

}
