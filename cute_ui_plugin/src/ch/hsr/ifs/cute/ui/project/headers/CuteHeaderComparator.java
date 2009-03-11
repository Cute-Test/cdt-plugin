/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.headers;

import java.util.Comparator;


/**
 * @author egraf
 *
 */
public class CuteHeaderComparator implements Comparator<ICuteHeaders> {
	
	public int compare(ICuteHeaders ch1, ICuteHeaders ch2) {
		double dif= ch1.getVersionNumber() - ch2.getVersionNumber();
		if(dif < 0.001)return 0;
		if(dif < 0)return 1;
		return -1;
	}

}
