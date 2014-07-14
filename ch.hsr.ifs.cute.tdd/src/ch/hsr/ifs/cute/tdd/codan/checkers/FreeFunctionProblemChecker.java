/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class FreeFunctionProblemChecker extends AbstractTDDChecker {

	public static final String ERR_ID_FunctionResolutionProblem = "ch.hsr.ifs.cute.tdd.codan.checkers.FunctionResolutionProblem";
	public static final String ERR_ID_FunctionResolutionProblem_STATIC = "ch.hsr.ifs.cute.tdd.codan.checkers.FunctionResolutionProblem_STATIC";
	public static final String ERR_ID_NamespaceMemberResolutionProblem = "ch.hsr.ifs.cute.tdd.codan.checkers.NamespaceMemberResolutionProblem";

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new FreeFunctionProblemVisitor());
	}

	class FreeFunctionProblemVisitor extends AbstractResolutionProblemVisitor {
		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (TddHelper.isFunctionCall(name.getParent())) {
				handleFunctionResolutionProblem(name, problemBinding);
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

	private void handleFunctionResolutionProblem(IASTName name, IProblemBinding problemBinding) {
		if (problemBinding.getCandidateBindings().length == 0) {
			if (name instanceof ICPPASTQualifiedName) {
				CPPASTQualifiedName qname = (CPPASTQualifiedName) name;
				handleQualifiedName(name, qname);
			} else {
				String missingName = new String(name.getSimpleID());
				reportMissingFunction(name, missingName);
			}
		}
	}

	private void handleQualifiedName(IASTName name, CPPASTQualifiedName qname) {
		boolean isTypeMember = false;
		IASTName lastName = qname.getLastName();
		if (!(lastName instanceof ICPPASTTemplateId)) {
			reportMissingFunction(name, lastName, isTypeMember);
		}
		for (ICPPASTNameSpecifier partname : qname.getQualifier()) {
			final IBinding partBinding = partname.resolveBinding();
			isTypeMember = partBinding instanceof ICPPClassType;
			if (partBinding instanceof IProblemBinding) {
				return;
			}
		}
	}

	private void reportMissingFunction(IASTName name, IASTName partname, boolean isTypeMember) {
		String missingName = new String(name.getSimpleID());
		if (isTypeMember) {
			reportMissingStaticMember(partname, missingName);
		} else {
			reportMissingNamespaceFunction(partname, missingName);
		}
	}

	private void reportMissingNamespaceFunction(IASTName name, String missingName) {
		CodanArguments ca = new CodanArguments(missingName, missingName, ":memberfunc");
		reportProblem(ERR_ID_NamespaceMemberResolutionProblem, name.getLastName(), ca.toArray());
	}

	private void reportMissingFunction(IASTName name, String missingName) {
		CodanArguments ca = new CodanArguments(missingName, missingName, ":freefunc");
		reportProblem(ERR_ID_FunctionResolutionProblem, name.getLastName(), ca.toArray());
	}

	private void reportMissingStaticMember(IASTName partname, String missingName) {
		CodanArguments ca = new CodanArguments(missingName, missingName, ":staticfreefunc");
		reportProblem(ERR_ID_FunctionResolutionProblem_STATIC, partname, ca.toArray());
	}
}
