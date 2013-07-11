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
package ch.hsr.ifs.cdt.namespactor.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

/**
 * @authors kunz@ideadapt.net, Jules Weder
 * */
public class CopyTemplateIdFactory extends TemplateIdFactory{

	public CopyTemplateIdFactory(ICPPASTTemplateId templateId){
		super(templateId);
	}

	@Override
	protected ICPPASTNamedTypeSpecifier createNamedDeclSpec(IASTDeclSpecifier vDeclSpecifier) {
		ICPPASTNamedTypeSpecifier newDeclSpec = factory.newNamedTypeSpecifier(null);
		IASTName specName = ((ICPPASTNamedTypeSpecifier)vDeclSpecifier).getName();

		// qualify the name of the specifier if it has nothing todo with a template id
		if(!isOrContainsTemplateId(specName)){
			IASTName qnameNode = specName;
			newDeclSpec.setName(qnameNode.copy());
		}
		return newDeclSpec;
	}

	@SuppressWarnings("restriction")
	@Override
	protected ICPPASTQualifiedName modifyTemplateId(ICPPASTTemplateId vTemplId) {
		return factory.newQualifiedName();
	}
}