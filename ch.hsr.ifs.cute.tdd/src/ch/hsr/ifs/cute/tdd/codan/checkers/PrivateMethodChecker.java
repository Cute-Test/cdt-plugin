/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseDeclSpecifier;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class PrivateMethodChecker extends AbstractTDDChecker {

	public static final String ERR_ID_PrivateMethodChecker_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.PrivateMethodChecker_HSR"; //$NON-NLS-1$

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				ICPPMember member = getMember(name);
				if (member == null) {
					return PROCESS_CONTINUE;
				}
				CPPASTBaseDeclSpecifier type = TddHelper.getAncestorOfType(name, CPPASTBaseDeclSpecifier.class);
				if (type != null) {
					return PROCESS_CONTINUE;
				}
				final ICPPClassType owner = (ICPPClassType) member.getOwner();
				final ICPPMember[] methods = owner.getAllDeclaredMethods();
				final ICPPMember[] fields = owner.getDeclaredFields();
				if (ArrayUtil.contains(methods, member) || ArrayUtil.contains(fields, member)) {
					if (member.getVisibility() == ICPPMember.v_private) {
						IType typeOfContext = findTypeOfContext(name);
						if (owner.isSameType(typeOfContext)) {
							return PROCESS_CONTINUE;
						}
						String memberName = new String(name.getSimpleID());
						CodanArguments ca = new CodanArguments(memberName, memberName + Messages.PrivateMethodChecker_1, ":visibility"); //$NON-NLS-1$
						reportProblem(ERR_ID_PrivateMethodChecker_HSR, name.getLastName(), ca.toArray());
					}// else if (member.getVisibility() == ICPPMember.v_protected) {
						// Not implemented
						//}
				}
				return PROCESS_CONTINUE;
			}

			private IType findTypeOfContext(IASTName name) {
				IASTNode ancestor = name;
				while (ancestor != null) {
					if (ancestor instanceof ICPPASTFunctionDefinition) {
						final IASTName functionName = ((ICPPASTFunctionDefinition) ancestor).getDeclarator().getName();
						final IBinding functionBinding = functionName.resolveBinding();
						final IBinding owner = functionBinding.getOwner();
						if (owner instanceof IType) {
							return (IType) owner;
						}
					}
					ancestor = ancestor.getParent();
				}

				return null;
			}

			private ICPPMember getMember(IASTName name) {
				if (!(name.getParent() instanceof ICPPASTFieldReference)) {
					return null;
				}
				IBinding binding = name.resolveBinding();
				if (!(binding instanceof ICPPMember)) {
					return null;
				}
				return (ICPPMember) binding;
			}
		});
	}
}
