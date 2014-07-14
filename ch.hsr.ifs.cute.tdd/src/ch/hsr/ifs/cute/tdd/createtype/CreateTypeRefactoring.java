/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createtype;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.TddCRefactoring;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class CreateTypeRefactoring extends TddCRefactoring {

	private final String className;
	private boolean isTemplateSituation;
	private final ArrayList<ICPPASTSimpleTypeTemplateParameter> listOfArgs;
	private final CodanArguments ca;

	public CreateTypeRefactoring(ITextSelection textSelection, CodanArguments ca) {
		super(textSelection);
		this.ca = ca;
		this.className = ca.getName();
		this.listOfArgs = createTemplateArgs();
	}

	private ArrayList<ICPPASTSimpleTypeTemplateParameter> createTemplateArgs() {
		char startChar = 'T';
		ArrayList<ICPPASTSimpleTypeTemplateParameter> result = new ArrayList<ICPPASTSimpleTypeTemplateParameter>();
		String args = ca.getTemplateArgs();
		if (!(args.isEmpty())) {
			int templateCount = args.split(",").length;
			for (int i = 0; i < templateCount; i++, startChar++) {
				isTemplateSituation = true;
				String newName = startChar + "";
				CPPASTSimpleTypeTemplateParameter templparam = new CPPASTSimpleTypeTemplateParameter();
				templparam.setDefaultType(null);
				templparam.setName(new CPPASTName(newName.toCharArray()));
				templparam.setParameterType(ICPPASTSimpleTypeTemplateParameter.st_typename);
				result.add(templparam);
			}
		}
		return result;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
		IASTTranslationUnit localunit = refactoringContext.getAST(tu, pm);
		IASTName namenearselection = localunit.getNodeSelector(null).findEnclosingName(getSelection().getOffset(), getSelection().getLength());
		if (namenearselection == null) {
			namenearselection = localunit.getNodeSelector(null).findFirstContainedName(getSelection().getOffset(), getSelection().getLength());
		}
		IASTNode newType;

		newType = createTemplatedType(className);

		IASTNode insertionPoint = getInsertionPoint(localunit, namenearselection, refactoringContext);

		if (insertionPoint instanceof CPPASTCompositeTypeSpecifier || insertionPoint instanceof ICPPASTNamespaceDefinition) {
			TddHelper.writeDefinitionTo(collector, insertionPoint, newType);
		} else {
			ASTRewrite rewrite = collector.rewriterForTranslationUnit(localunit);
			rewrite.insertBefore(insertionPoint.getParent(), insertionPoint, newType, null);
		}
	}

	public static IASTNode getInsertionPoint(IASTTranslationUnit localunit, IASTName namenearselection, CRefactoringContext context) {
		IASTNode insertionPoint = null;
		if (namenearselection instanceof ICPPASTQualifiedName) {
			insertionPoint = TddHelper.getNestedInsertionPoint(localunit, (ICPPASTQualifiedName) namenearselection, context);
		} else {
			IASTNode parent = namenearselection.getParent();
			if (parent instanceof ICPPASTQualifiedName) {
				insertionPoint = TddHelper.getNestedInsertionPoint(localunit, (ICPPASTQualifiedName) parent, context);
			}
		}
		if (insertionPoint == null) {
			insertionPoint = getFunctionDefinition(namenearselection);
			if (insertionPoint == null) {
				insertionPoint = localunit.getDeclarations()[0];
			}
		}
		return insertionPoint;
	}

	private IASTNode createTemplatedType(String className) {
		IASTSimpleDeclaration simpleddec = createType(className);
		IASTNode result = simpleddec;
		CPPASTTemplateDeclaration templdecl;
		if (isTemplateSituation) {
			templdecl = new CPPASTTemplateDeclaration(simpleddec);
			result = templdecl;
			for (ICPPASTSimpleTypeTemplateParameter templparam : listOfArgs) {
				templdecl.addTemplateParameter(templparam);
			}
		}
		return result;
	}

	private IASTSimpleDeclaration createType(String className) {
		ICPPASTCompositeTypeSpecifier struct = new CPPASTCompositeTypeSpecifier();
		IASTSimpleDeclaration decl = new CPPASTSimpleDeclaration(struct);
		struct.setKey(1);
		struct.setName(new CPPASTName(className.toCharArray()));
		struct.setParent(decl);
		return decl;
	}

	public static IASTNode getFunctionDefinition(IASTName declaratorName) {
		IASTNode func = TddHelper.getAncestorOfType(declaratorName, ICPPASTFunctionDefinition.class);
		if (func == null) {
			func = TddHelper.getAncestorOfType(declaratorName, ICPPASTCompositeTypeSpecifier.class);
			return TddHelper.getAncestorOfType(func, IASTSimpleDeclaration.class);
		}
		IASTNode template = TddHelper.getLastOfSameAncestor(func.getParent(), ICPPASTTemplateDeclaration.class);
		if (template != null) {
			func = template;
		}
		return func;
	}
}
