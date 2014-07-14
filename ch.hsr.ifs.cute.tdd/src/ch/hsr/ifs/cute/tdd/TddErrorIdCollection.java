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

	public static final String ERR_ID_PrivateMethodChecker = PrivateMethodChecker.ERR_ID_PrivateMethodChecker;
	public static final String ERR_ID_TypeResolutionProblem = TypeResolutionProblemChecker.ERR_ID_TypeResolutionProblem;
	public static final String ERR_ID_FunctionResolutionProblem = FreeFunctionProblemChecker.ERR_ID_FunctionResolutionProblem;
	public static final String ERR_ID_FunctionResolutionProblem_STATIC = FreeFunctionProblemChecker.ERR_ID_FunctionResolutionProblem_STATIC;
	public static final String ERR_ID_MethodResolutionProblem = MemberFunctionProblemChecker.ERR_ID_MethodResolutionProblem;
	public static final String ERR_ID_MissingConstructorResolutionProblem = MissingConstructorChecker.ERR_ID_MissingConstructorResolutionProblem;
	public static final String ERR_ID_VariableResolutionProblem = VariableResolutionProblemChecker.ERR_ID_VariableResolutionProblem;
	public static final String ERR_ID_MemberVariableResolutionProblem = VariableResolutionProblemChecker.ERR_ID_MemberVariableResolutionProblem;
	public static final String ERR_ID_OperatorResolutionProblem = MissingOperatorChecker.ERR_ID_OperatorResolutionProblem;
	public static final String ERR_ID_InvalidArguments_FREE = WrongArgumentChecker.ERR_ID_InvalidArguments_FREE;
	public static final String ERR_ID_InvalidArguments = WrongArgumentChecker.ERR_ID_InvalidArguments;
	public static final String ERR_ID_NamespaceResolutionProblem = MissingNamespaceChecker.ERR_ID_NamespaceResolutionProblem;
	public static final String ERR_ID_NamespaceMemberResolutionProblem = FreeFunctionProblemChecker.ERR_ID_NamespaceMemberResolutionProblem;

	public static boolean isOperator(IMarker marker) {
		return CodanProblemMarker.getProblemId(marker).equals(ERR_ID_OperatorResolutionProblem);
	}

	public static boolean isStatic(IMarker marker) {
		return CodanProblemMarker.getProblemId(marker).equals(ERR_ID_FunctionResolutionProblem_STATIC);
	}
}
