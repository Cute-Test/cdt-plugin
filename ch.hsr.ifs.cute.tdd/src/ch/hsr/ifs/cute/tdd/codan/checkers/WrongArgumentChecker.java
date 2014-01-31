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
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;
import ch.hsr.ifs.cute.tdd.addArgument.AddArgumentQFGenerator;
import ch.hsr.ifs.cute.tdd.addArgument.AddArgumentRefactoring;

public class WrongArgumentChecker extends AbstractTDDChecker {

	private static final String COMMA_SPACE = ", ";
	public static final String ERR_ID_InvalidArguments_HSR = "ch.hsr.eclipse.cdt.codan.checkers.InvalidArguments_HSR";
	public static final String ERR_ID_InvalidArguments_FREE_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.InvalidArguments_FREE_HSR";
	private static final String EMPTY_STRING = "";

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new WrongArgumentProblemVisitor());
	}

	public class WrongArgumentProblemVisitor extends AbstractResolutionProblemVisitor {

		@Override
		protected void reactOnProblemBinding(IProblemBinding problemBinding, IASTName name) {
			if (!TddHelper.isMethod(name) || problemBinding.getCandidateBindings().length < 1) {
				return;
			}
			IASTFunctionCallExpression call = TddHelper.getAncestorOfType(name, IASTFunctionCallExpression.class);
			List<IASTInitializerClause> oldArgs = Arrays.asList(call.getArguments());
			IBinding[] candidates = problemBinding.getCandidateBindings();
			candidates = removeDuplicates(candidates);
			String contextString = EMPTY_STRING;
			String ERR_ID = null;
			int argNr;
			for (argNr = 0; argNr < candidates.length; argNr++) {
				IBinding b = candidates[argNr];
				if (b instanceof ICPPMethod) {
					ERR_ID = ERR_ID_InvalidArguments_HSR;
				} else {
					ERR_ID = ERR_ID_InvalidArguments_FREE_HSR;
				}
				if (b instanceof ICPPFunction) {
					ICPPFunction candidate = (ICPPFunction) b;
					List<ICPPParameter> newParams = Arrays.asList(candidate.getParameters());
					List<IASTInitializerClause> newArgs = AddArgumentRefactoring.getNewArguments(oldArgs, newParams);
					if (newArgs == null || newArgs.size() == oldArgs.size()) {
						return;
					}
					if (argNr > 0) {
						contextString += ":candidate ";
					}
					contextString += getContextString(name, newArgs, candidate);
				}
			}
			String missingName = new String(name.getLastName().getSimpleID());
			String message = missingName;
			CodanArguments ca = new CodanArguments(missingName, message, ":candidate");
			ca.setCandidate(argNr);
			ca.setCandidates(contextString);
			assert (ca.toArray().length >= AddArgumentQFGenerator.REQUIRED_MARKER_ARGUMENTS);
			reportProblem(ERR_ID, name.getLastName(), ca.toArray());
		}

		// There are two markers generated
		private IBinding[] removeDuplicates(IBinding[] candidates) {
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			for (int i = 0; i < candidates.length; i++) {
				for (int j = i + 1; j < candidates.length; j++) {
					ICPPParameter[] arglist1 = ((ICPPFunction) candidates[i]).getParameters();
					ICPPParameter[] arglist2 = ((ICPPFunction) candidates[j]).getParameters();
					boolean same = false;
					if (arglist1.length == 0 && arglist2.length == 0) {
						same = true;
						continue;
					}
					for (int k = 0; arglist1.length == arglist2.length && k < arglist1.length; k++) {
						ICPPParameter param1 = arglist1[k];
						ICPPParameter param2 = arglist2[k];
						same = param1.getType().isSameType(param2.getType());
						if (!same) {
							break;
						}
					}
					if (same) {
						toRemove.add(j);
					}
				}
			}
			ArrayList<ICPPBinding> result = new ArrayList<ICPPBinding>();
			for (int i = 0; i < candidates.length; i++) {
				if (!toRemove.contains(i)) {
					result.add((ICPPBinding) candidates[i]);
				}
			}
			return result.toArray(new IBinding[result.size()]);
		}
	}

	private String getContextString(IASTName name, List<IASTInitializerClause> newArgs, ICPPFunction candidate) {
		return getArgumentNames(name, candidate) + AddArgumentQFGenerator.SEPARATOR + getParameterNames(name, candidate);
	}

	private String getArgumentNames(IASTName name, ICPPFunction candidate) {
		String result = EMPTY_STRING;
		IASTFunctionCallExpression call = TddHelper.getAncestorOfType(name, IASTFunctionCallExpression.class);
		List<IASTInitializerClause> oldArgs = Arrays.asList(call.getArguments());
		ICPPParameter[] newParams = candidate.getParameters();
		if (oldArgs.size() > newParams.length) {
			result += AddArgumentQFGenerator.REMOVE_ARGUMENTS;
			for (int i = 0; i < oldArgs.size(); i++) {
				if (i >= newParams.length) {
					result += ASTTypeUtil.getType(TypeHelper.getTypeOf(oldArgs.get(i))) + COMMA_SPACE;
				}
			}
		} else {
			result = AddArgumentQFGenerator.ADD_ARGUMENTS;
			for (int i = 0; i < newParams.length; i++) {
				if (i >= oldArgs.size()) {
					result += ASTTypeUtil.getType(newParams[i].getType()) + COMMA_SPACE;
				}
			}
		}
		if (result.endsWith(COMMA_SPACE)) {
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	private String getParameterNames(IASTName name, ICPPFunction candidate) {
		String result = AddArgumentQFGenerator.PARAMETERS;
		if (candidate.getParameters().length == 0) {
			return result;
		}
		IBinding binding = name.resolveBinding();
		if (binding instanceof IProblemBinding) {
			for (ICPPParameter p : candidate.getParameters()) {
				result += ASTTypeUtil.getType(p.getType()) + COMMA_SPACE;
			}
		}
		return result.substring(0, result.length() - 2);
	}
}
