/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class MemberFunctionProblemChecker extends AbstractTDDChecker {

	public static final String ERR_ID_MethodResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MethodResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new MemberFunctionBindingProblemVisitor());
	}

	class MemberFunctionBindingProblemVisitor extends AbstractResolutionProblemVisitor {

		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (TddHelper.nameNotFoundProblem(problemBinding) && isFieldReference(name)) {
				handleMemberResolutionProblem(name, problemBinding);
			}
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTTemplateId) {
				return PROCESS_SKIP;
			} else {
				return super.visit(name);
			}
		}
	}

	private boolean isFieldReference(IASTName name) {
		return name.getParent() instanceof ICPPASTFieldReference;
	}

	private void handleMemberResolutionProblem(IASTName name, IProblemBinding problemBinding) {
		if (TddHelper.isMethod(name)) {
			IASTFieldReference member = TddHelper.getAncestorOfType(name, IASTFieldReference.class);
			IASTExpression expression = member.getFieldOwner();
			if (!isOfTypeWithMembers(expression)) {
				return;
			}
			if (problemBinding.getCandidateBindings().length == 0) {
				String missingName = new String(name.getSimpleID());
				CodanArguments ca = new CodanArguments(missingName, missingName, ":memberfunc"); //$NON-NLS-1$
				reportProblem(ERR_ID_MethodResolutionProblem_HSR, name.getLastName(), ca.toArray());
			}
		}
	}

	private boolean isOfTypeWithMembers(IASTExpression expression) {
		IType expressionType = SemanticUtil.getNestedType(expression.getExpressionType(), ALLCVQ | TDEF | REF);
		return expressionType instanceof ICPPClassType;
	}
}
