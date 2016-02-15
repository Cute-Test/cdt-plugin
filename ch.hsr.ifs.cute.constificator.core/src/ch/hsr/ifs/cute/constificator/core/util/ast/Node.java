package ch.hsr.ifs.cute.constificator.core.util.ast;

import static ch.hsr.ifs.cute.constificator.core.deciders.util.Constants.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Arrays.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

import ch.hsr.ifs.cute.constificator.core.util.functional.IBinaryPredicate;
import ch.hsr.ifs.cute.constificator.core.util.functional.IUnaryFunction;
import ch.hsr.ifs.cute.constificator.core.util.functional.IUnaryPredicate;

public class Node {

	public static IASTName[] getReferences(ICPPASTName name) {
		ICPPASTTranslationUnit tu = (ICPPASTTranslationUnit) name.getTranslationUnit();
		IBinding binding = name.resolveBinding();
		return tu.getReferences(binding);
	}

	public static <T> boolean anyOfDescendingFrom(Class<T> ancestorCls, ICPPASTName name,
			IBinaryPredicate<T, IASTName> pred) {
		IASTName[] references = getReferences(name);

		for (IASTName reference : references) {

			IASTNodeLocation[] nodeLocations = reference.getNodeLocations();

			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					continue;
				}
			}

			if (isDescendendOf(ancestorCls, reference)) {
				T ancestor = getAncestorOf(ancestorCls, reference);

				if (ancestor instanceof IASTNode) {
					if (pred.holdsFor(ancestor, reference)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static <T> void forEachIf(T[] suspects, IUnaryPredicate<T> pred, IUnaryFunction<T> func) {
		if (suspects == null || pred == null || func == null) {
			return;
		}

		for (T current : suspects) {
			if (pred.evaluate(current)) {
				func.call(current);
			}
		}
	}

	public static boolean declaresLValueReferenceToPointer(ICPPASTDeclarator declarator) {
		IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();

		if (pointerOperators == null || pointerOperators.length <= 1) {
			return false;
		}

		IASTPointerOperator lastOperator = pointerOperators[pointerOperators.length - 1];
		ICPPASTReferenceOperator referenceOperator = as(ICPPASTReferenceOperator.class, lastOperator);

		if (referenceOperator != null && !referenceOperator.isRValueReference()) {
			return as(IASTPointer.class, pointerOperators[pointerOperators.length - 2]) != null;
		}

		return false;
	}

	public static boolean declaresLValueReferenceToObject(ICPPASTDeclarator declarator) {
		IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();

		if (pointerOperators == null || pointerOperators.length != 1) {
			return false;
		}

		IASTPointerOperator lastOperator = pointerOperators[pointerOperators.length - 1];
		ICPPASTReferenceOperator referenceOperator = as(ICPPASTReferenceOperator.class, lastOperator);

		if (referenceOperator == null || referenceOperator.isRValueReference()) {
			return false;
		}

		IASTSimpleDeclaration declaration = as(IASTSimpleDeclaration.class, declarator.getParent());

		if (declaration != null) {
			ICPPASTDeclSpecifier specifier = as(ICPPASTDeclSpecifier.class, declaration.getDeclSpecifier());
			return specifier != null;
		}

		return false;
	}

	public static IASTName resolveToName(IASTNode node) {
		if (node instanceof IASTIdExpression) {
			return ((IASTIdExpression) node).getName();
		}

		return null;
	}

	public static boolean isDereferencedNTimes(IASTName name, int nofDereferences) {
		ICPPVariable variable;
		if ((variable = as(ICPPVariable.class, name.resolveBinding())) == null) {
			return false;
		}
		IType type = variable.getType();

		if (isReference(type)) {
			--nofDereferences;
		}

		if (isArray(type)) {
			ICPPASTArraySubscriptExpression sub = getAncestorOf(ICPPASTArraySubscriptExpression.class, name);

			while (sub != null) {
				--nofDereferences;
				sub = getAncestorOf(ICPPASTArraySubscriptExpression.class, sub.getParent());
			}
		}

		ICPPASTFieldReference fieldReference = getAncestorOf(ICPPASTFieldReference.class, name);
		while (fieldReference != null) {
			if (fieldReference.isPointerDereference()) {
				--nofDereferences;
			}
			fieldReference = getAncestorOf(ICPPASTFieldReference.class, fieldReference.getParent());
		}

		ICPPASTUnaryExpression unary = getAncestorOf(ICPPASTUnaryExpression.class, name);
		while (unary != null) {
			switch (unary.getOperator()) {
			case IASTUnaryExpression.op_star:
				--nofDereferences;
				break;
			case IASTUnaryExpression.op_amper:
				++nofDereferences;
				break;
			default:
				if (isAnyOf(unary.getOperator(), MODIFYING_BINARY_OPERATORS)
						|| isAnyOf(unary.getOperator(), MODIFYING_UNARY_OPERATORS)) {
					unary = null;
				}
				break;
			}

			if (unary != null) {
				unary = as(ICPPASTUnaryExpression.class, unary.getParent());
			}
		}

		return nofDereferences == 0;
	}

	public static boolean isUsedToTakeAddressOf(IASTName reference) {

		boolean takesAddress = false;
		IASTUnaryExpression unary = getAncestorOf(IASTUnaryExpression.class, reference);
		while (unary != null) {
			int op = unary.getOperator();

			if (op == ICPPASTUnaryExpression.op_amper) {
				takesAddress = true;
			} else if (op == ICPPASTUnaryExpression.op_star) {
				takesAddress = false;
			}

			unary = getAncestorOf(IASTUnaryExpression.class, unary.getParent());
		}
		return takesAddress;
	}

	public static ICPPASTName getResultingName(ICPPASTInitializerClause clause) {

		IASTExpressionList list;
		if ((list = getDescendendOf(IASTExpressionList.class, clause)) != null) {
			return getResultingName((ICPPASTInitializerClause) list.getExpressions()[list.getExpressions().length - 1]);
		} else if (clause instanceof IASTIdExpression) {
			return as(ICPPASTName.class, ((IASTIdExpression) clause).getName());
		} else if (clause instanceof IASTUnaryExpression) {
			return getResultingName((ICPPASTInitializerClause) ((IASTUnaryExpression) clause).getOperand());
		}

		return null;
	}

	public static List<Integer> getArgumentIndicesFor(IASTInitializerClause[] arguments, ICPPASTName name,
			IUnaryPredicate<IASTName> condition) {
		List<Integer> indices = new ArrayList<>();

		if (name != null) {
			for (int index = 0; index < arguments.length; ++index) {
				ICPPASTInitializerClause clause = as(ICPPASTInitializerClause.class, arguments[index]);

				if (clause != null) {
					ICPPASTName argumentName = getResultingName(clause);

					if (argumentName != null && argumentName.resolveBinding().equals(name.resolveBinding())) {
						if ((condition != null && condition.evaluate(argumentName)) || condition == null) {
							indices.add(index);
						}
					}
				}
			}
		}

		return indices;
	}

	public static ICPPASTName getNameForFunction(IASTExpression functionNameExpression) {
		if (functionNameExpression instanceof IASTIdExpression) {
			return as(ICPPASTName.class, ((IASTIdExpression) functionNameExpression).getName());
		} else if (functionNameExpression instanceof IASTFieldReference) {
			return as(ICPPASTName.class, ((IASTFieldReference) functionNameExpression).getFieldName());
		} else if (functionNameExpression instanceof IASTUnaryExpression) {
			return getNameForFunction(((IASTUnaryExpression) functionNameExpression).getOperand());
		}

		return null;
	}

}
