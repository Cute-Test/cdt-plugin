/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.TddHelper;

@SuppressWarnings("restriction")
public class VariableResolutionProblemChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID_VariableResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.VariableResolutionProblem_HSR"; //$NON-NLS-1$
	public static final String ERR_ID_MemberVariableResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MemberVariableResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new VariableResolutionProblemHandler());
	}

	class VariableResolutionProblemHandler extends AbstractResolutionProblemVisitor {
		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (TddHelper.isInvalidType(problemBinding)) {
				return;
			}
			if (TddHelper.nameNotFoundProblem(problemBinding)) {
				IASTNode upmostName = (IASTNode) TddHelper.getLastOfSameAncestor(name, IASTName.class);
				IASTNode parent = upmostName.getParent();
				if (parent instanceof IASTNamedTypeSpecifier) {
					return;
				}
				if (parent instanceof ICPPASTBaseSpecifier) {
					return;
				}
				if (parent instanceof CPPASTTemplateId) {
					CPPASTTemplateId templid = (CPPASTTemplateId) name.getParent();
					IBinding b = templid.getBinding();
					if (b instanceof IProblemBinding && TddHelper.isInvalidType((IProblemBinding)b)){
						return;
					}
				}
				if (TddHelper.isFunctionCall(parent)) {
					return;
				}
				if (problemBinding.getCandidateBindings().length > 0) {
					return;
				}
				String missingName = name.getBinding().getName();
				if (parent instanceof ICPPASTConstructorChainInitializer) {
					CodanArguments args = new CodanArguments(missingName, Messages.VariableResolutionProblemChecker_2, ":memberVariable"); //$NON-NLS-1$
					reportProblem(TddErrorIdCollection.ERR_ID_MemberVariableResolutionProblem_HSR, name, args.toArray());
				} else if (parent instanceof IASTFieldReference) {
					if (ToggleNodeHelper.getAncestorOfType(name, IASTFunctionCallExpression.class) != null) {
						return;
					}
					CodanArguments args = new CodanArguments(missingName, Messages.VariableResolutionProblemChecker_4, ":memberVariable"); //$NON-NLS-1$
					reportProblem(TddErrorIdCollection.ERR_ID_MemberVariableResolutionProblem_HSR, name, args.toArray());
				} else {
					String message = Messages.VariableResolutionProblemChecker_6 + missingName + Messages.VariableResolutionProblemChecker_7;
					CodanArguments ca = new CodanArguments(missingName, message, ":variable"); //$NON-NLS-1$
					reportProblem(ERR_ID_VariableResolutionProblem_HSR, name, ca.toArray());
				}
			}
		}
	}
}