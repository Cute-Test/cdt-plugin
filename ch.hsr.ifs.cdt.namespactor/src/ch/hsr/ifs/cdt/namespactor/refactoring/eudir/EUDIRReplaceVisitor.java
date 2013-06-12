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
package ch.hsr.ifs.cdt.namespactor.refactoring.eudir;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;

import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EURefactoringContext;
import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EUReplaceVisitor;
import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EUTemplateIdFactory;

/**
 * @author Jules Weder
 * */
public class EUDIRReplaceVisitor extends EUReplaceVisitor {
	
	public EUDIRReplaceVisitor(EURefactoringContext context) {
		this.context = context;
	}

	@Override
	protected void removeUnqualifiedUsingDirective(IASTName name) {
		if(name instanceof IASTName && name.getParent() instanceof ICPPASTUsingDirective){
			IASTName replacementName = buildReplacementName(name);
			if(replacementName != null && replacementName.getLastName() == null){
				removeUselessUsingDirective(name);
			}
		}
	}
	
	@Override
	protected IASTName searchNamesFor(IASTName name, IASTName[] names) {
		if(name == null){
			return null;
		}
		for (IASTName iastName : names) {
			if(iastName.resolveBinding().equals(name.resolveBinding())){
				return iastName;
			}
		}
		return null;
	}
	
	@Override
	protected boolean isReplaceCandidate(IASTName foundName, IASTName name, IASTName[] names) {
		return foundName != null;
	}
	
	@Override
	protected ICPPASTQualifiedName buildReplacementTemplate(IASTName iastName) {
		ICPPASTQualifiedName replaceName;
		EUTemplateIdFactory templateBuilder = new EUDIRTemplateIdFactory((ICPPASTTemplateId) iastName, context);
		replaceName = (ICPPASTQualifiedName) templateBuilder.buildTemplate();
		return replaceName;
	}
	
	@Override
	protected boolean isNameFound(IASTName foundName, IASTName iastName) {
		return iastName.equals(foundName);
	}
}