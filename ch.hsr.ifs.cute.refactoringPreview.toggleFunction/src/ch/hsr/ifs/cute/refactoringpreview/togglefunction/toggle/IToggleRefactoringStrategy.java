/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.refactoringpreview.togglefunction.toggle;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;

@SuppressWarnings("restriction")
public interface IToggleRefactoringStrategy {
	public void run(ModificationCollector modifications);
}