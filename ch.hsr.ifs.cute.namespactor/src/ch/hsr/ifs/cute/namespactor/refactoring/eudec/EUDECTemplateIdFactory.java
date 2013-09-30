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
package ch.hsr.ifs.cute.namespactor.refactoring.eudec;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

import ch.hsr.ifs.cute.namespactor.refactoring.eu.EURefactoringContext;
import ch.hsr.ifs.cute.namespactor.refactoring.eu.EUTemplateIdFactory;

/**
 * @author Jules Weder
 * */
public class EUDECTemplateIdFactory extends EUTemplateIdFactory{

	public EUDECTemplateIdFactory(ICPPASTTemplateId templateId, EURefactoringContext context){
		super(templateId, context);
	}

	@Override
	protected void precedeWithQualifiers(ICPPASTQualifiedName replaceName, IASTName[] names, IASTName templateName) {
		IASTName type = selectedType;
		if(selectedType instanceof ICPPASTTemplateId){
			type = ((ICPPASTTemplateId)selectedType).getTemplateName();
		}
		if(!type.resolveBinding().equals(templateName.resolveBinding())){
			for (IASTName iastName : names) {
				if(iastName instanceof ICPPASTTemplateId){
					iastName = ((ICPPASTTemplateId)iastName).getTemplateName();
				}
				if(iastName.resolveBinding().equals(templateName.resolveBinding())){
					break;
				}
				replaceName.addName(iastName.copy());
			}
		}

	}

}