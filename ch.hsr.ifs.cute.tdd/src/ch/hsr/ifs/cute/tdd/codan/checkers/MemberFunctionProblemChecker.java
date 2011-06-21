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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

@SuppressWarnings("restriction")
public class MemberFunctionProblemChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID_MethodResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MethodResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new MemberFunctionBindingProblemVisitor());
	}

	class MemberFunctionBindingProblemVisitor extends AbstractResolutionProblemVisitor {

		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding,
				IASTName name) {
			if (TddHelper.nameNotFoundProblem(problemBinding) && isFieldReference(name)) {
				handleMemberResolutionProblem(name, problemBinding);
			}
		}
	}

	private boolean isFieldReference(IASTName name) {
		return name.getParent() instanceof ICPPASTFieldReference;
	}

	private void handleMemberResolutionProblem(IASTName name, IProblemBinding problemBinding) {
		if (TddHelper.isMethod(name)) {
			ICPPASTFieldReference fref = ToggleNodeHelper.getAncestorOfType(name, IASTFieldReference.class);
			CPPASTIdExpression variable = (CPPASTIdExpression) fref.getFieldOwner();
			if (variable.getName().resolveBinding() instanceof IProblemBinding) {
				return;
			}
			if (problemBinding.getCandidateBindings().length == 0) {
				String missingName = new String(name.getSimpleID());
				CodanArguments ca = new CodanArguments(missingName, Messages.MemberFunctionProblemChecker_1 + missingName + Messages.MemberFunctionProblemChecker_2, ":memberfunc"); //$NON-NLS-1$
				reportProblem(ERR_ID_MethodResolutionProblem_HSR, name.getLastName(), ca.toArray());
			}
		}
	}
}
