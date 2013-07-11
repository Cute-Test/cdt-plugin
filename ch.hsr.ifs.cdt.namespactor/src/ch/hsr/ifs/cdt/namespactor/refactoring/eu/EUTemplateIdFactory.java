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
package ch.hsr.ifs.cdt.namespactor.refactoring.eu;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;

import ch.hsr.ifs.cdt.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.TemplateIdFactory;

/**
 * @author Jules Weder
 * */
@SuppressWarnings("restriction")
public abstract class EUTemplateIdFactory extends TemplateIdFactory {

	protected IASTName selectedName;
	protected IASTName selectedType;

	public EUTemplateIdFactory(ICPPASTTemplateId templateId, EURefactoringContext context) {
		super(templateId);
		this.selectedName = context.startingNamespaceName;
		this.selectedType = context.startingTypeName;
	}

	protected void buildReplaceName(ICPPASTQualifiedName replaceName, IASTName[] names) {
		boolean start = false;
		for (IASTName iastName : names) {
			IBinding binding = iastName.resolveBinding();
			if(start){
				if(iastName instanceof ICPPASTTemplateId){
					break;
				}
				if(binding instanceof ICPPNamespace ||  binding instanceof ICPPClassType){
					replaceName.addName(iastName.copy());
				}
			}
			if(binding.equals(selectedName.resolveBinding())){
				start = true;
			}
		}
	}

	protected void precedeWithQualifiers(ICPPASTQualifiedName replaceName, IASTName[] names, IASTName templateName) {
	
	}

	@Override
	protected ICPPASTNamedTypeSpecifier createNamedDeclSpec(IASTDeclSpecifier vDeclSpecifier) {
		ICPPASTNamedTypeSpecifier newDeclSpec = factory.newNamedTypeSpecifier(null);
		IASTName specName = ((ICPPASTNamedTypeSpecifier)vDeclSpecifier).getName();
		ICPPASTQualifiedName replaceName = ASTNodeFactory.getDefault().newQualifiedName();
	
		if(specName instanceof ICPPASTQualifiedName){
			IASTName[] names = ((ICPPASTQualifiedName)specName).getNames();
			precedeWithQualifiers(replaceName, names, specName.getLastName());
			buildReplaceName(replaceName, names);	
			newDeclSpec.setName(replaceName);
		}else{
			newDeclSpec.setName(specName.copy());			
		}
	
		return newDeclSpec;
	}

	@Override
	protected ICPPASTQualifiedName modifyTemplateId(ICPPASTTemplateId vTemplId) {
		ICPPASTQualifiedName replaceName = ASTNodeFactory.getDefault().newQualifiedName();
	
		IASTName[] names = null;
		if(vTemplId.getParent() instanceof ICPPASTQualifiedName){
			names = ((ICPPASTQualifiedName)vTemplId.getParent()).getNames();
		}else{
			names = new IASTName[]{((IASTName)vTemplId.getParent()).getLastName()};
		}
		precedeWithQualifiers(replaceName, names, vTemplId.getTemplateName());
		buildReplaceName(replaceName, names);	
		return replaceName;
	}

}
