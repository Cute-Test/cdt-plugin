package ch.hsr.ifs.constificator.core.deciders.common;

import static ch.hsr.ifs.constificator.core.deciders.util.FunctionUtil.*;
import static ch.hsr.ifs.constificator.core.deciders.util.NonPointerUtil.*;
import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.trait.Types.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;

import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NodeDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NullDecision;
import ch.hsr.ifs.constificator.core.deciders.util.Common;
import ch.hsr.ifs.constificator.core.util.functional.IUnaryPredicate;
import ch.hsr.ifs.constificator.core.util.type.Pair;
import ch.hsr.ifs.constificator.core.util.type.Truelean;

public class NonPointer {

	private static List<IUnaryPredicate<ICPPASTName>> m_definitive = new ArrayList<>(8);

	static {
		m_definitive.add((n) -> isLeftHandSideInModifyingBinaryExpression(n));
		m_definitive.add((n) -> isOperandInModifyingUnaryExpression(n));
		m_definitive.add((n) -> isUsedToBindLValueReferenceToNonConst(n));
		m_definitive.add((n) -> addressIsAssignedToPointerToNonConst(n));
		m_definitive.add((n) -> addressIsPassedAsReferenceToPointerToNonConst(n));
		m_definitive.add((n) -> addressIsUsedToBindReferenceToPointerToNonConst(n));
		m_definitive.add((n) -> nonConstMemberFunctionCalledOn(n));
		m_definitive.add((n) -> nonConstMemberAccessed(n));
		m_definitive.add((n) -> isReturnedAsReferenceToNonConst(n));
		m_definitive.add((n) -> isReturnedAsPointerToNonConst(n));
	}

	public static IDecision decide(ICPPASTDeclarator declarator, ICPPASTName name, IType type) {
		IASTSimpleDeclaration variableDeclaration = null;
		IASTParameterDeclaration parameterDeclaration = null;

		if ((parameterDeclaration = getAncestorOf(IASTParameterDeclaration.class, declarator)) == null
				&& (variableDeclaration = getAncestorOf(IASTSimpleDeclaration.class, declarator)) == null) {
			return new NullDecision();
		}

		if (Common.isUsedInTemplateSpecialization(name) || Common.isUsedInVaragsFunction(name)
				|| Common.isUsedInUnknownFunction(name)) {
			return new NullDecision();
		}

		IDecision decision = variableDeclaration != null ? new NodeDecision(variableDeclaration.getDeclSpecifier())
				: new NodeDecision(parameterDeclaration.getDeclSpecifier());
		boolean canConstify = !isConst(type, 0);
		boolean mightBeConstifiable = false;

		Iterator<IUnaryPredicate<ICPPASTName>> it = m_definitive.iterator();
		while (it.hasNext() && canConstify) {
			canConstify &= !it.next().evaluate(name);
		}

		if (canConstify) {
			Pair<Boolean, Pair<IASTName, Integer>> result = isPassedAsLValueReferenceToNonConst(name); // C3

			if (result.first()) {
				canConstify = false;
				IASTName function = result.second().first();
				Integer parameterIndex = result.second().second();
				mightBeConstifiable = hasConstOverload((ICPPASTName) function, parameterIndex, 1);
			}
		}

		if (canConstify) {
			Pair<Boolean, Pair<IASTName, Integer>> result = addressIsPassedAsPointerToNonConst(name); // C5

			if (result.first()) {
				canConstify = false;
				IASTName function = result.second().first();
				Integer parameterIndex = result.second().second();
				mightBeConstifiable = hasConstOverload((ICPPASTName) function, parameterIndex, 1);
			}
		}

		decision.decide(canConstify ? Truelean.YES : mightBeConstifiable ? Truelean.MAYBE : Truelean.NO);
		return decision;
	}

}
