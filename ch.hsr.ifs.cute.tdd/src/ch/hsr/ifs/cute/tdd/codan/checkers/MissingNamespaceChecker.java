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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import ch.hsr.ifs.cute.tdd.CodanArguments;

public class MissingNamespaceChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID_NamespaceResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.NamespaceResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new MissingNamespaceVisitor());
	}

	class MissingNamespaceVisitor extends AbstractResolutionProblemVisitor	 {
		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (name instanceof ICPPASTQualifiedName) {
				ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
				for(IASTName partname: qname.getNames()) {
					if (partname == qname.getLastName()) {
						return;
					}
					IBinding b = partname.resolveBinding();
					if (b instanceof IProblemBinding) {
						String nodename = new String(partname.getSimpleID());
						CodanArguments ca = new CodanArguments(nodename, Messages.MissingNamespaceChecker_1 + nodename, ":namespace"); //$NON-NLS-1$
						reportProblem(ERR_ID_NamespaceResolutionProblem_HSR, partname, ca.toArray());
						return;
					}
				}
			}
		}
	}
}