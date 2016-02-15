package ch.hsr.ifs.cute.constificator.core.deciders.common;

import static ch.hsr.ifs.cute.constificator.core.deciders.util.FunctionUtil.*;
import static ch.hsr.ifs.cute.constificator.core.deciders.util.NonPointerUtil.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.NodeDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.NullDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.util.Common;
import ch.hsr.ifs.cute.constificator.core.util.functional.IUnaryPredicate;
import ch.hsr.ifs.cute.constificator.core.util.type.Pair;
import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public class NonPointer {

	private static List<IUnaryPredicate<ICPPASTName>> definitive = new ArrayList<>(8);

	static {
		definitive.add((n) -> isLeftHandSideInModifyingBinaryExpression(n));
		definitive.add((n) -> isOperandInModifyingUnaryExpression(n));
		definitive.add((n) -> isUsedToBindLValueReferenceToNonConst(n));
		definitive.add((n) -> addressIsAssignedToPointerToNonConst(n));
		definitive.add((n) -> addressIsPassedAsReferenceToPointerToNonConst(n));
		definitive.add((n) -> addressIsUsedToBindReferenceToPointerToNonConst(n));
		definitive.add((n) -> nonConstMemberFunctionCalledOn(n));
		definitive.add((n) -> nonConstMemberAccessed(n));
		definitive.add((n) -> isReturnedAsReferenceToNonConst(n));
		definitive.add((n) -> isReturnedAsPointerToNonConst(n));
	}

	public static IDecision decide(ICPPASTDeclarator declarator, ICPPASTName name, IType type) {
		IASTSimpleDeclaration variableDeclaration = null;
		IASTParameterDeclaration parameterDeclaration = null;
		
		ASTRewriteCache cache = new ASTRewriteCache(declarator.getTranslationUnit().getIndex());

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
		
		
		boolean typeIsConst = isConst(type, 0);
		boolean canConstifyNonPtr = !typeIsConst;
		
		if(parameterDeclaration != null) {
			boolean nonPointerIsConst = parameterDeclaration.getDeclSpecifier().isConst();
			canConstifyNonPtr = !typeIsConst || (typeIsConst && !nonPointerIsConst);
		}
		
		boolean mightBeConstifiable = false;

		Iterator<IUnaryPredicate<ICPPASTName>> it = definitive.iterator();
		while (it.hasNext() && canConstifyNonPtr) {
			canConstifyNonPtr &= !it.next().evaluate(name);
		}

		if (canConstifyNonPtr) {
			Pair<Boolean, Pair<IASTName, Integer>> result = isPassedAsLValueReferenceToNonConst(name); // C3

			if (result.first()) {
				canConstifyNonPtr = false;
				IASTName function = result.second().first();
				Integer parameterIndex = result.second().second();
				mightBeConstifiable = hasConstOverload((ICPPASTName) function, parameterIndex, 1, cache);
			}
		}

		if (canConstifyNonPtr) {
			Pair<Boolean, Pair<IASTName, Integer>> result = addressIsPassedAsPointerToNonConst(name); // C5

			if (result.first()) {
				canConstifyNonPtr = false;
				IASTName function = result.second().first();
				Integer parameterIndex = result.second().second();
				mightBeConstifiable = hasConstOverload((ICPPASTName) function, parameterIndex, 1, cache);
			}
		}

		decision.decide(canConstifyNonPtr ? Truelean.YES : mightBeConstifiable ? Truelean.MAYBE : Truelean.NO);
		return decision;
	}

}
