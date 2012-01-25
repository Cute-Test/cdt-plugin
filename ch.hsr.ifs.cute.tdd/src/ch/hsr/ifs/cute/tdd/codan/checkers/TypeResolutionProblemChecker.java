/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class TypeResolutionProblemChecker extends AbstractTDDChecker {

	public static final String ERR_ID_TypeResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.TypeResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new TypeResolutionProblemVisitor());
	}

	class TypeResolutionProblemVisitor extends AbstractResolutionProblemVisitor {

		private static final String TEMPLATE_ARGUMENT_SEPARATOR = ",";
		ArrayList<IASTNode> reportedNames = new ArrayList<IASTNode>();

		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			//do not report B<int> twice
			if (name.getParent() != null && name.getParent() instanceof ICPPASTTemplateId) {
				if (reportedNames.contains(name.getParent())) {
					return;
				}
			}

			final String missingName = new String(name.getSimpleID());
			final ICPPASTNamedTypeSpecifier nts = TddHelper.getAncestorOfType(name, CPPASTNamedTypeSpecifier.class);
			if (nts != null && nts.isTypename()) {
				return;
			}
			CodanArguments ca = createCodanArguments(name, missingName, nts);
			if (TddHelper.nameNotFoundProblem(problemBinding)) {
				ICPPASTBaseSpecifier base = TddHelper.getAncestorOfType(name, ICPPASTBaseSpecifier.class);
				if (base != null || name.getParent() instanceof IASTNamedTypeSpecifier) {
					reportType(name, ca);
				}
			} else if (TddHelper.isInvalidType(problemBinding)) {
				reportType(name, ca);
			}
		}

		private CodanArguments createCodanArguments(IASTName name, final String missingName, final ICPPASTNamedTypeSpecifier nts) {
			String strategy = ":type"; //$NON-NLS-1$
			final String message = Messages.TypeResolutionProblemChecker_1 + missingName + Messages.TypeResolutionProblemChecker_2;

			ICPPASTTemplateId templateId = extractTemplateId(name, nts);
			if (templateId != null) {
				strategy = ":templtype"; //$NON-NLS-1$
				String args = composeArgumentString(templateId);
				CodanArguments ca = new CodanArguments(missingName, message, strategy, args);
				return ca;
			} else {
				CodanArguments ca = new CodanArguments(missingName, message, strategy);
				return ca;
			}
		}

		private void reportType(IASTName name, CodanArguments ca) {
			reportedNames.add(name);
			reportProblem(ERR_ID_TypeResolutionProblem_HSR, name, ca.toArray());
		}

		private String composeArgumentString(ICPPASTTemplateId templateId) {
			String args = ""; //$NON-NLS-1$
			for (IASTNode node : templateId.getTemplateArguments()) {
				if (node instanceof CPPASTTypeId) {
					IASTDeclSpecifier declspec = ((CPPASTTypeId) node).getDeclSpecifier();
					args += declspec.getRawSignature() + TEMPLATE_ARGUMENT_SEPARATOR; //$NON-NLS-1$
				}
			}
			if (!args.isEmpty()) {
				args = args.substring(0, args.length() - TEMPLATE_ARGUMENT_SEPARATOR.length());
			}
			return args;
		}

		private ICPPASTTemplateId extractTemplateId(IASTName name, final ICPPASTNamedTypeSpecifier nts) {
			ICPPASTTemplateId templid = null;
			if (nts != null && nts.getName() instanceof ICPPASTTemplateId) {
				templid = (ICPPASTTemplateId) nts.getName();
			} else if (name instanceof ICPPASTTemplateId) {
				templid = (ICPPASTTemplateId) name;
			}
			return templid;
		}
	}
}
