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
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;

import ch.hsr.ifs.cute.namespactor.refactoring.eu.EURefactoringContext;
import ch.hsr.ifs.cute.namespactor.refactoring.eu.EUTemplateIdFactory;

/**
 * @author Jules Weder
 * */
public class EUDecTemplateIdFactory extends EUTemplateIdFactory {

	public EUDecTemplateIdFactory(ICPPASTTemplateId templateId, EURefactoringContext context) {
		super(templateId, context);
	}
	@Override
	protected void buildReplaceName(ICPPASTQualifiedName replaceName, IASTName[] names) {
		boolean start = false;
		for (IASTName iastName : names) {
			IBinding binding = ((IASTName)iastName.getOriginalNode()).resolveBinding();
			if (start) {
				if (binding instanceof ICPPNamespace || binding instanceof ICPPClassType) {
					replaceName.addName(iastName.copy(CopyStyle.withLocations));
				}
				if (iastName instanceof ICPPASTTemplateId) {
					replaceName.setLastName(((ICPPASTTemplateId)iastName).copy(CopyStyle.withLocations));
					break;
				}
			}
			if (binding.equals(selectedName.resolveBinding())) {
				start = true;
			}
		}
	}

	@Override
	protected void precedeWithQualifiers(ICPPASTQualifiedName replaceName, IASTName[] names, IASTName templateName) {
		IASTName type = selectedType;
		if (selectedType instanceof ICPPASTTemplateId) {
			type = ((ICPPASTTemplateId) selectedType).getTemplateName();
			if (templateName instanceof ICPPASTTemplateId){
				templateName = ((ICPPASTTemplateId) templateName).getTemplateName();
			}
		}
		if (! ((IASTName)type.getOriginalNode()).resolveBinding().equals(((IASTName)templateName.getOriginalNode()).resolveBinding())) {
			for (IASTName iastName : names) {
				if (iastName instanceof ICPPASTTemplateId) {
					iastName = ((ICPPASTTemplateId) iastName).getTemplateName();
				}
				if (((IASTName)iastName.getOriginalNode()).resolveBinding().equals(((IASTName)templateName.getOriginalNode()).resolveBinding())) {
					break;
				}
				replaceName.addName(iastName.copy(CopyStyle.withLocations));
			}
		}
	}
}