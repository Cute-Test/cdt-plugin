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
package ch.hsr.ifs.cute.namespactor.refactoring;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

import ch.hsr.ifs.cute.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cute.namespactor.astutil.NSNodeHelper;

/**
 * @author kunz@ideadapt.net, Jules Weder
 * */
@SuppressWarnings("restriction")
public abstract class TemplateIdFactory extends ASTVisitor {

	protected IASTNode currNode;
	protected ASTNodeFactory factory = ASTNodeFactory.getDefault();
	protected ICPPASTTemplateId templateId;

	public TemplateIdFactory(ICPPASTTemplateId templateId) {
		this.templateId = templateId;
		shouldVisitNames = true;
		shouldVisitTypeIds = true;
		shouldVisitExpressions = true;
	}

	public static boolean isOrContainsTemplateId(IASTName name) {
		if (name instanceof ICPPASTTemplateId) {
			return true;
		}
		if (name instanceof ICPPASTQualifiedName) {
			for (IASTName n : ((ICPPASTQualifiedName) name).getNames()) {
				if (n instanceof ICPPASTTemplateId) {
					return true;
				}
			}
		}
		return false;
	}

	public IASTName buildTemplate() {
//		templateId.accept(this);
//		return this.getTemplateId();
		IASTName newTemplateId=null;
		if (templateId.getParent() instanceof ICPPASTQualifiedName){
			newTemplateId =((ICPPASTQualifiedName)templateId.getParent()).copy(CopyStyle.withLocations);
		} else {
			newTemplateId = templateId.copy(CopyStyle.withLocations);
		}
		newTemplateId.accept(new ASTVisitor() {
			{
				shouldVisitNames=true;
				shouldVisitTypeIds=true;
			}
			public int visit(IASTName name){
				if (name instanceof ICPPASTTemplateId){
					ICPPASTQualifiedName newqname = modifyTemplateId((ICPPASTTemplateId)name);
					((ICPPASTTemplateId) name).setTemplateName(newqname);
				}
				return super.visit(name);
			}

			public int visit(IASTTypeId typeId) {
				IASTDeclSpecifier vDeclSpecifier = typeId.getDeclSpecifier();
				IASTDeclSpecifier newDeclSpec = null;

				if (vDeclSpecifier instanceof ICPPASTNamedTypeSpecifier) {
					newDeclSpec = createNamedDeclSpec(vDeclSpecifier);
				} else if (vDeclSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
					newDeclSpec = createSimpleDeclSpec(vDeclSpecifier);
				}
				if (newDeclSpec!=null) typeId.setDeclSpecifier(newDeclSpec);
				return super.visit(typeId);
			}

		});
		return newTemplateId;//this.getTemplateId();
	}

	@Override
	public int visit(IASTExpression expression) {
		// TODO Auto-generated method stub
		IASTExpression newex=expression.copy();
		
		return super.visit(expression);
	}
	@Override
	public int visit(IASTName name) {
		if (name instanceof ICPPASTTemplateId) {

			ICPPASTTemplateId vTemplId = (ICPPASTTemplateId) name;
			ICPPASTQualifiedName nameNode = modifyTemplateId(vTemplId); // problem....
			ICPPASTTemplateId newTemplId = factory.newTemplateId(vTemplId.getTemplateName().copy()); // problem...

			nameNode.addName(newTemplId);

			if (currNode instanceof ICPPASTNamedTypeSpecifier) {
				((ICPPASTNamedTypeSpecifier) currNode).setName(nameNode);
			}
			currNode = newTemplId;
		}

		return super.visit(name);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		if (currNode instanceof ICPPASTTemplateId) {

			IASTDeclSpecifier vDeclSpecifier = typeId.getDeclSpecifier();
			IASTDeclSpecifier newDeclSpec = null;

			if (vDeclSpecifier instanceof ICPPASTNamedTypeSpecifier) {
				newDeclSpec = createNamedDeclSpec(vDeclSpecifier);
			} else if (vDeclSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
				newDeclSpec = createSimpleDeclSpec(vDeclSpecifier);
			}

			if (newDeclSpec != null) {
				((ICPPASTTemplateId) currNode).addTemplateArgument(factory.newTypeId(newDeclSpec, null));
				currNode = newDeclSpec; // loses parent template id. must be a stack.
			}
		}
		return super.visit(typeId);
	}
	//TODO: must also cover expressions in template arguments. (psommerl)

	protected IASTDeclSpecifier createSimpleDeclSpec(IASTDeclSpecifier vDeclSpecifier) {

		ICPPASTSimpleDeclSpecifier newDeclSpec = factory.newSimpleDeclSpecifier();
		newDeclSpec.setType(((ICPPASTSimpleDeclSpecifier) vDeclSpecifier).getType());
		return newDeclSpec;
	}

	protected abstract ICPPASTNamedTypeSpecifier createNamedDeclSpec(IASTDeclSpecifier vDeclSpecifier);

	protected abstract ICPPASTQualifiedName modifyTemplateId(ICPPASTTemplateId vTemplId);

	protected IASTName getTemplateId() {
		return (IASTName) NSNodeHelper.getRoot(currNode);
	}
}