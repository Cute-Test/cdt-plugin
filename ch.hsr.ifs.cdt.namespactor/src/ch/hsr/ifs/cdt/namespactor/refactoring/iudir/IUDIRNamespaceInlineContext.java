/******************************************************************************
* Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/
package ch.hsr.ifs.cdt.namespactor.refactoring.iudir;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * @author kunz@ideadapt.net
 * */
public class IUDIRNamespaceInlineContext {
	public IASTName usingName = null;
	public IIndexName namespaceDefName = null;
}
