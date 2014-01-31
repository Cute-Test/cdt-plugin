/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.cute.tdd.codan.checkers.FreeFunctionProblemChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.MemberFunctionProblemChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.MissingConstructorChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.MissingNamespaceChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.MissingOperatorChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.PrivateMethodChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.TypeResolutionProblemChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.VariableResolutionProblemChecker;
import ch.hsr.ifs.cute.tdd.codan.checkers.WrongArgumentChecker;

@SuppressWarnings("restriction")
public class TddErrorIdCollection {

	public static final String ERR_ID_PrivateMethodChecker_HSR = PrivateMethodChecker.ERR_ID_PrivateMethodChecker_HSR;
	public static final String ERR_ID_TypeResolutionProblem_HSR = TypeResolutionProblemChecker.ERR_ID_TypeResolutionProblem_HSR;
	public static final String ERR_ID_FunctionResolutionProblem_HSR = FreeFunctionProblemChecker.ERR_ID_FunctionResolutionProblem_HSR;
	public static final String ERR_ID_FunctionResolutionProblem_STATIC_HSR = FreeFunctionProblemChecker.ERR_ID_FunctionResolutionProblem_STATIC_HSR;
	public static final String ERR_ID_MethodResolutionProblem_HSR = MemberFunctionProblemChecker.ERR_ID_MethodResolutionProblem_HSR;
	public static final String ERR_ID_MissingConstructorResolutionProblem_HSR = MissingConstructorChecker.ERR_ID_MissingConstructorResolutionProblem_HSR;
	public static final String ERR_ID_VariableResolutionProblem_HSR = VariableResolutionProblemChecker.ERR_ID_VariableResolutionProblem_HSR;
	public static final String ERR_ID_MemberVariableResolutionProblem_HSR = VariableResolutionProblemChecker.ERR_ID_MemberVariableResolutionProblem_HSR;
	public static final String ERR_ID_OperatorResolutionProblem_HSR = MissingOperatorChecker.ERR_ID_OperatorResolutionProblem_HSR;
	public static final String ERR_ID_InvalidArguments_FREE_HSR = WrongArgumentChecker.ERR_ID_InvalidArguments_FREE_HSR;
	public static final String ERR_ID_InvalidArguments_HSR = WrongArgumentChecker.ERR_ID_InvalidArguments_HSR;
	public static final String ERR_ID_NamespaceResolutionProblem_HSR = MissingNamespaceChecker.ERR_ID_NamespaceResolutionProblem_HSR;
	public static final String ERR_ID_NamespaceMemberResolutionProblem_HSR = FreeFunctionProblemChecker.ERR_ID_NamespaceMemberResolutionProblem_HSR;

	public static boolean isOperator(IMarker marker) {
		return CodanProblemMarker.getProblemId(marker).equals(ERR_ID_OperatorResolutionProblem_HSR);
	}

	public static boolean isStatic(IMarker marker) {
		return CodanProblemMarker.getProblemId(marker).equals(ERR_ID_FunctionResolutionProblem_STATIC_HSR);
	}
}
