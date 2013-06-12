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
package ch.hsr.ifs.cdt.namespactor.refactoring.eudec;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

import ch.hsr.ifs.cdt.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EURefactoringContext;
import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EUReplaceVisitor;
import ch.hsr.ifs.cdt.namespactor.refactoring.eu.EUTemplateIdFactory;

/**
 * @author Jules Weder
 * */
public class EUDECReplaceVisitor extends EUReplaceVisitor {

	public EUDECReplaceVisitor(EURefactoringContext context) {
		this.context = context;
	}
	
	@Override
	protected IASTName searchNamesFor(IASTName name, IASTName[] names) {
		if(name == null){
			return null;
		}
		for (IASTName iastName : names) {
			if(name instanceof ICPPASTTemplateId && iastName instanceof ICPPASTTemplateId){
				if(NSNameHelper.isSameNameInTemplateId((ICPPASTTemplateId)name, (ICPPASTTemplateId)iastName)){
					return iastName;
				}
			}
			if(iastName.resolveBinding().equals(name.resolveBinding())){
				return iastName;
			}
		}
		return null;
	}
	
	@Override
	protected boolean isReplaceCandidate(IASTName foundName, IASTName name, IASTName[] names) {
		return foundName != null && isSameName(name.getLastName(), names);
	}
	
	private boolean isSameName(IASTName name, IASTName[] names) {
		if(context.startingTypeName != null){
			for (IASTName iastName : names) {
				if(context.startingTypeName.resolveBinding().equals(iastName.resolveBinding())){
					return true;
				}
				if(name instanceof ICPPASTTemplateId && iastName instanceof ICPPASTTemplateId){
					return NSNameHelper.isSameNameInTemplateId((ICPPASTTemplateId)name, (ICPPASTTemplateId)iastName);
				}
			}
			return false;
		}
		IBinding nameBinding = name.resolveBinding();
		IBinding selectionBinding = context.selectedLastName.resolveBinding();
		
		return nameBinding.getOwner().equals(selectionBinding.getOwner()) 
			   && nameBinding.getName().equals(selectionBinding.getName());
	}
	
	@Override
	protected ICPPASTQualifiedName buildReplacementTemplate(IASTName iastName) {
		ICPPASTQualifiedName replaceName;
		EUTemplateIdFactory templateBuilder = new EUDECTemplateIdFactory((ICPPASTTemplateId) iastName, context);
		replaceName = (ICPPASTQualifiedName) templateBuilder.buildTemplate();
		return replaceName;
	}
	
	@Override
	protected boolean isNameFound(IASTName foundName, IASTName iastName) {
		if(foundName instanceof ICPPASTTemplateId && iastName instanceof ICPPASTTemplateId){
			return NSNameHelper.isSameNameInTemplateId((ICPPASTTemplateId)foundName, (ICPPASTTemplateId) iastName);
		}else{
			return iastName.equals(foundName);
		}
	}
	
	@Override
	protected void buildFullyQualifiedReplaceName(ICPPASTQualifiedName replaceName, IASTName[] names) {
		for (IASTName iastName : names) {
			replaceName.addName(iastName.copy());
		}
	}
}