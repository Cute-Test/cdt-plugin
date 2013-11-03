/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class VariableResolutionProblemChecker extends AbstractTDDChecker {

	public static final String ERR_ID_VariableResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.VariableResolutionProblem_HSR";
	public static final String ERR_ID_MemberVariableResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MemberVariableResolutionProblem_HSR";

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new VariableResolutionProblemHandler());
	}

	class VariableResolutionProblemHandler extends AbstractResolutionProblemVisitor {

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTTemplateId) {
				return PROCESS_SKIP;
			} else {
				return super.visit(name);
			}
		}

		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (TddHelper.nameNotFoundProblem(problemBinding)) {
				if (name instanceof ICPPASTQualifiedName) {
					return;
				}
				if (isIdentifyingFunctionCallPart(name)) {
					return;
				}
				if (TddHelper.isInvalidType(problemBinding)) {
					return;
				}
				if (!TddHelper.isLastPartOfQualifiedName(name)) {
					return;
				}
				if (TddHelper.hasUnresolvableNameQualifier(name)) {
					return;
				}
				IASTNode upmostName = TddHelper.getLastOfSameAncestor(name, IASTName.class);
				IASTNode parent = upmostName.getParent();
				if (parent instanceof IASTNamedTypeSpecifier) {
					return;
				}
				if (parent instanceof ICPPASTBaseSpecifier) {
					return;
				}
				if (upmostName instanceof ICPPASTQualifiedName) {
					handleStaticMemberVariable(name, (ICPPASTQualifiedName) upmostName);
					return;
				}
				if (parent instanceof CPPASTTemplateId) {
					CPPASTTemplateId templid = (CPPASTTemplateId) name.getParent();
					IBinding b = templid.getBinding();
					if (b instanceof IProblemBinding && TddHelper.isInvalidType((IProblemBinding) b)) {
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
					reportMissingMemberVariable(name, missingName);
				} else if (parent instanceof IASTFieldReference) {
					if (TddHelper.getAncestorOfType(name, IASTFunctionCallExpression.class) != null) {
						return;
					}
					reportMissingMemberVariable(name, missingName);
				} else {
					CodanArguments ca = new CodanArguments(missingName, missingName, ":variable");
					reportProblem(ERR_ID_VariableResolutionProblem_HSR, name, ca.toArray());
				}
			}
		}

		private boolean isIdentifyingFunctionCallPart(IASTName name) {
			return (name.getPropertyInParent() == IASTFieldReference.FIELD_NAME || (name.getParent() instanceof ICPPASTQualifiedName && TddHelper
					.isLastPartOfQualifiedName(name))) && TddHelper.getAncestorOfType(name, IASTFunctionCallExpression.class) != null;
		}

		private void reportMissingMemberVariable(IASTName name, String missingName) {
			CodanArguments args = new CodanArguments(missingName, missingName, ":memberVariable");
			reportProblem(TddErrorIdCollection.ERR_ID_MemberVariableResolutionProblem_HSR, name, args.toArray());
		}

		private void handleStaticMemberVariable(IASTName name, ICPPASTQualifiedName parent) {
			reportMissingMemberVariable(name, name.getBinding().getName());
		}

	}
}