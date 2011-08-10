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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

@SuppressWarnings("restriction")
public class FreeFunctionProblemChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID_FunctionResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.FunctionResolutionProblem_HSR"; //$NON-NLS-1$
	public static final String ERR_ID_FunctionResolutionProblem_STATIC_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.FunctionResolutionProblem_STATIC_HSR"; //$NON-NLS-1$
	
	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new FreeFunctionProblemVisitor());
	}

	class FreeFunctionProblemVisitor extends AbstractResolutionProblemVisitor {
		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (TddHelper.isFunctionCall(name.getParent())) {
				handleFunctionResolutionProblem(name, problemBinding);
			}
		}
	}

	private void handleFunctionResolutionProblem(IASTName name, IProblemBinding problemBinding) {
		if (problemBinding.getCandidateBindings().length == 0) {
			if (name instanceof ICPPASTQualifiedName) {
				CPPASTQualifiedName qname = (CPPASTQualifiedName) name;
				for(IASTName partname: qname.getNames()) {
					if (partname == qname.getLastName()) {
						String missingName = new String(name.getSimpleID());
						CodanArguments ca = new CodanArguments(missingName, Messages.FreeFunctionProblemChecker_2 + missingName, ":staticfreefunc"); //$NON-NLS-1$
						reportProblem(ERR_ID_FunctionResolutionProblem_STATIC_HSR, partname, ca.toArray());
					}
					if (partname.resolveBinding() instanceof IProblemBinding) {
						return;
					}
				}
			}
			String missingName = new String(name.getSimpleID());
			CodanArguments ca = new CodanArguments(missingName, Messages.FreeFunctionProblemChecker_4 + missingName, ":freefunc"); //$NON-NLS-1$
			reportProblem(ERR_ID_FunctionResolutionProblem_HSR, name.getLastName(), ca.toArray());
		}
	}
}
