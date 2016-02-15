package ch.hsr.ifs.constificator.core.deciders.common;

import static ch.hsr.ifs.constificator.core.deciders.util.FunctionUtil.*;
import static ch.hsr.ifs.constificator.core.deciders.util.NonPointerUtil.*;
import static ch.hsr.ifs.constificator.core.deciders.util.PointerUtil.*;
import static ch.hsr.ifs.constificator.core.util.ast.Node.*;
import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NodeDecision;
import ch.hsr.ifs.constificator.core.deciders.util.Common;
import ch.hsr.ifs.constificator.core.util.functional.IBinaryPredicate;
import ch.hsr.ifs.constificator.core.util.functional.IUnaryPredicate;
import ch.hsr.ifs.constificator.core.util.type.Truelean;

@SuppressWarnings("restriction")
public class Pointer {

	private static List<IUnaryPredicate<ICPPASTName>> m_pointerRules = new ArrayList<>(8);
	private static List<IBinaryPredicate<ICPPASTName, Integer>> m_pointeeRules = new ArrayList<>(6);

	static {
		m_pointerRules.add((n) -> isLeftHandSideInModifyingBinaryExpression(n));
		m_pointerRules.add((n) -> isOperandInModifyingUnaryExpression(n));
		m_pointerRules.add((n) -> isPassedAsLValueReferenceToNonConstPointer(n));
		m_pointerRules.add((n) -> isUsedToBindLValueReferenceToNonConstPointer(n));
		m_pointerRules.add((n) -> addressIsPassedAsPointerToNonConstPointer(n));
		m_pointerRules.add((n) -> addressIsAssignedToPointerToNonConstPointer(n));
		m_pointerRules.add((n) -> addressIsUsedToInitializePointerToNonConstPointer(n));
		m_pointerRules.add((n) -> addressIsPassedAsReferenceToPointerToNonConstPointer(n));
		m_pointerRules.add((n) -> addressIsUsedToBindReferenceToPointerToNonConstPointer(n));

		m_pointeeRules.add((n, i) -> pointeeIsLeftHandSideInModifyingBinaryExpression(n, i));
		m_pointeeRules.add((n, i) -> pointeeIsLeftHandSideInModifyingUnaryExpression(n, i));
		m_pointeeRules.add((n, i) -> pointeeIsPassedAsReferenceToNonConst(n, i));
		m_pointeeRules.add((n, i) -> pointeeIsUsedToBindReferenceToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isPassedToFunctionTakingPointerToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isAssignedToPointerToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isPassedToFunctionTakingReferenceToPointerToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isUsedToBindReferenceToPointerToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isUsedToInitializePointerToNonConst(n, i));
		m_pointeeRules.add((n, i) -> isInitializedUsingLessConstPointer(n, i));
		m_pointeeRules.add((n, i) -> nonConstMemberAccessedOnPointee(n, i));
	}

	public static List<IDecision> decide(ICPPASTDeclarator declarator, ICPPASTName name, IType type) {
		List<IDecision> decisions = new ArrayList<>(declarator.getPointerOperators().length + 1);

		if (Common.isUsedInTemplateSpecialization(name) || Common.isUsedInVaragsFunction(name)
				|| Common.isUsedInUnknownFunction(name)) {
			return decisions;
		}

		final IASTPointerOperator[] pointerOps = declarator.getPointerOperators();
		final int nofPointerOps = pointerOps.length;

		if (!(declarator instanceof IASTArrayDeclarator) && nofPointerOps > 0
				&& pointerOps[nofPointerOps - 1] instanceof IASTPointer) {
			boolean canConstifyPtr = !isConst(type, 0);

			Iterator<IUnaryPredicate<ICPPASTName>> it = m_pointerRules.iterator();
			while (it.hasNext() && canConstifyPtr) {
				canConstifyPtr &= !it.next().evaluate(name);
			}

			IDecision decision = new NodeDecision(pointerOps[nofPointerOps - 1]);
			decision.decide(canConstifyPtr ? Truelean.YES : Truelean.NO);
			decisions.add(decision);
		}

		final int arrayDimension = declarator instanceof IASTArrayDeclarator
				? ((IASTArrayDeclarator) declarator).getArrayModifiers().length : 0;

		int startLevel = arrayDimension > 0 ? 0 : 1;

		for (int level = startLevel; level <= nofPointerOps; ++level) {
			boolean canConstifyLevel;
			IASTNode currentNode = null;

			if (level < nofPointerOps) {
				currentNode = pointerOps[nofPointerOps - 1 - level];
				canConstifyLevel = !((IASTPointer) currentNode).isConst();
			} else if (declarator.getParent() instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration decl = as(IASTParameterDeclaration.class, declarator.getParent());
				currentNode = decl.getDeclSpecifier();
				canConstifyLevel = !decl.getDeclSpecifier().isConst();
			} else if (declarator.getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration decl = as(IASTSimpleDeclaration.class, declarator.getParent());
				currentNode = decl.getDeclSpecifier();
				canConstifyLevel = !decl.getDeclSpecifier().isConst();
			} else {
				break;
			}

			if (nofPointerOps > 0 && pointerOps[nofPointerOps - 1] instanceof ICPPASTReferenceOperator && level > 1
					&& canConstifyLevel) {
				IASTInitializer initializer = declarator.getInitializer();

				IASTName initializerName = null;

				if (initializer instanceof IASTEqualsInitializer) {
					initializerName = resolveToName(((IASTEqualsInitializer) initializer).getInitializerClause());
				} else if (initializer instanceof ICPPASTInitializerList) {
					initializerName = resolveToName(((ICPPASTInitializerList) initializer).getClauses()[0]);
				}

				if (initializerName != null) {
					IBinding initializerBinding = initializerName.getBinding();

					if (initializerBinding instanceof CPPVariable) {
						CPPVariable declared = (CPPVariable) name.resolveBinding();
						canConstifyLevel = isConst(((CPPVariable) initializerBinding).getType(), level - 1)
								|| isConst(declared.getType(), level - 1);
					}
				}
			}

			Iterator<IBinaryPredicate<ICPPASTName, Integer>> it2 = m_pointeeRules.iterator();
			while (it2.hasNext() && canConstifyLevel) {
				canConstifyLevel &= !it2.next().holdsFor(name, level + arrayDimension);
			}

			if (canConstifyLevel && declarator.getParent() instanceof IASTParameterDeclaration) {
				IASTFunctionDeclarator function = getAncestorOf(IASTFunctionDeclarator.class, name);

				CPPParameter binding;
				if ((binding = as(CPPParameter.class, name.resolveBinding())) != null) {
					binding.getParameterPosition();
					canConstifyLevel &= !hasConstOverload((ICPPASTName) function.getName(),
							binding.getParameterPosition(), level);
				}
			}

			IDecision decision = new NodeDecision(currentNode);
			decision.decide(canConstifyLevel ? Truelean.YES : Truelean.NO);
			decisions.add(decision);
		}

		return decisions;
	}

}
