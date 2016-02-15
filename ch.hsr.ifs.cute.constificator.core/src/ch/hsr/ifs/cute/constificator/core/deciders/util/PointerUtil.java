package ch.hsr.ifs.cute.constificator.core.deciders.util;

import static ch.hsr.ifs.cute.constificator.core.deciders.util.Constants.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Node.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Arrays.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import ch.hsr.ifs.cute.constificator.core.util.ast.Node;

@SuppressWarnings("restriction")
public class PointerUtil {

	public static boolean addressIsAssignedToPointerToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTBinaryExpression.class, name,
				(ICPPASTBinaryExpression expr, IASTName reference) -> {
					while (expr != null && expr.getOperator() != ICPPASTBinaryExpression.op_assign) {
						expr = getAncestorOf(ICPPASTBinaryExpression.class, expr.getParent());
					}

					if (expr == null) {
						return false;
					}

					ICPPASTName lhsName;
					ICPPASTName rhsName;
					if ((lhsName = getDescendendOf(ICPPASTName.class, expr.getOperand1())) == null
							|| (rhsName = getDescendendOf(ICPPASTName.class, expr.getOperand2())) == null) {
						return false;
					}

					ICPPVariable lhs;
					ICPPVariable rhs;
					if ((lhs = as(ICPPVariable.class, lhsName.resolveBinding())) == null
							|| (rhs = as(ICPPVariable.class, rhsName.resolveBinding())) == null) {
						return false;
					}

					if (!rhs.equals(name.resolveBinding())) {
						return false;
					}

					IType lhsType = lhs.getType();
					IType rhsType = rhs.getType();

					if (pointerLevels(lhsType) != pointerLevels(rhsType) + 1) {
						return false;
					}

					return !isConst(lhsType, 1);
				});
	}

	public static boolean addressIsUsedToInitializePointerToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer init, IASTName reference) -> {
			if (isUsedToTakeAddressOf(reference)) {
				ICPPASTDeclarator declarator;
				if((declarator = getAncestorOf(ICPPASTDeclarator.class, init)) == null) {
					return false;
				}

				ICPPVariable lhs;

				if ((lhs = as(ICPPVariable.class, declarator.getName().resolveBinding())) == null) {
					return false;
				}

				return !isConst(lhs.getType(), 1);
			}

			return false;
		});
	}



	public static boolean addressIsPassedAsPointerToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression expression, IASTName reference) -> {
					List<Integer> argumentIndexes = getArgumentIndicesFor(expression.getArguments(), name,
							(n) -> isUsedToTakeAddressOf(n));

					ICPPASTName functionName;
					if ((functionName = getNameForFunction(expression.getFunctionNameExpression())) == null) {
						return false;
					}

					ICPPFunction function;
					if ((function = as(ICPPFunction.class, functionName.resolveBinding())) == null) {
						return false;
					}

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : argumentIndexes) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof IPointerType)) {
								continue;
							}

							IPointerType parameterType = (IPointerType) parameter.getType();
							IPointerType pointeeType = as(IPointerType.class, parameterType.getType());

							if (pointeeType != null && !pointeeType.isConst()) {
								return true;
							}
						}
					}

					return false;
				});
	}

	public static boolean addressIsPassedAsReferenceToPointerToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression ancestor, IASTName reference) -> {
					ICPPASTFunctionCallExpression expression = ancestor;

					if (isDescendendOf(ICPPASTFieldReference.class, reference)) {
						return false;
					}

					ICPPASTUnaryExpression unary = getAncestorOf(ICPPASTUnaryExpression.class, reference);

					if (unary == null || unary.getOperator() != IASTUnaryExpression.op_amper) {
						return false;
					}

					ArrayList<Integer> argumentIndexes = new ArrayList<>();
					IASTInitializerClause[] arguments = expression.getArguments();

					for (int i = 0; i < arguments.length; ++i) {
						ICPPASTUnaryExpression argument = as(ICPPASTUnaryExpression.class, arguments[i]);

						IASTIdExpression idExpression = getDescendendOf(IASTIdExpression.class, argument);

						if (idExpression != null
								&& idExpression.getName().getBinding().equals(reference.getBinding())) {
							argumentIndexes.add(i);
						}
					}

					IASTIdExpression functionName = (IASTIdExpression) expression.getFunctionNameExpression();
					IBinding binding = functionName.getName().resolveBinding();
					ICPPFunction function = as(ICPPFunction.class, binding);

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : argumentIndexes) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof ICPPReferenceType)) {
								continue;
							}

							ICPPReferenceType topLevelParameterType = (ICPPReferenceType) parameter.getType();

							if (!(topLevelParameterType.getType() instanceof IPointerType)) {
								continue;
							}

							IPointerType referencedType = (IPointerType) topLevelParameterType.getType();
							IPointerType pointeeType = as(IPointerType.class, referencedType.getType());

							if (pointeeType == null || !pointeeType.isConst()) {
								return true;
							}
						}
					}

					return false;
				});
	}

	public static boolean addressIsUsedToBindReferenceToPointerToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer ancestor, IASTName reference) -> {
			IASTInitializer initializer = ancestor;
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, initializer);
			IASTPointerOperator[] pointerOperators = declarator != null ? declarator.getPointerOperators() : null;
			ICPPASTUnaryExpression unary = getAncestorOf(ICPPASTUnaryExpression.class, reference);

			return unary != null && unary.getOperator() == IASTUnaryExpression.op_amper && pointerOperators != null
					&& pointerOperators.length > 2 && declaresLValueReferenceToPointer(declarator)
					&& as(IASTPointer.class, pointerOperators[pointerOperators.length - 3]) != null
					&& !as(IASTPointer.class, pointerOperators[pointerOperators.length - 3]).isConst();
		});
	}

	public static boolean isUsedToBindLValueReferenceToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer ancestor, IASTName reference) -> {
			IASTInitializer initializer = ancestor;
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, initializer);
			IASTPointerOperator[] pointerOperators = declarator != null ? declarator.getPointerOperators() : null;

			return pointerOperators != null && pointerOperators.length >= 2
					&& declaresLValueReferenceToPointer(declarator)
					&& as(IASTPointer.class, pointerOperators[pointerOperators.length - 2]) != null
					&& !as(IASTPointer.class, pointerOperators[pointerOperators.length - 2]).isConst();
		});
	}

	public static boolean isPassedAsLValueReferenceToNonConstPointer(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression ancestor, IASTName reference) -> {
					ICPPASTFunctionCallExpression expression = ancestor;

					if (isDescendendOf(ICPPASTUnaryExpression.class, reference)
							|| isDescendendOf(ICPPASTFieldReference.class, reference)) {
						return false;
					}

					List<Integer> indices = getArgumentIndicesFor(expression.getArguments(), name, (n) -> {
						return isDereferencedNTimes(n, 0);
					});

					ICPPASTName functionName;
					if ((functionName = getNameForFunction(expression.getFunctionNameExpression())) == null) {
						return false;
					}

					ICPPFunction function = as(ICPPFunction.class, functionName.resolveBinding());

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : indices) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof ICPPReferenceType)) {
								continue;
							}

							if (((ICPPReferenceType) parameter.getType()).isRValueReference()) {
								continue;
							}

							ICPPReferenceType parameterType = (ICPPReferenceType) parameter.getType();
							IPointerType pointerType = as(IPointerType.class, parameterType.getType());

							if (pointerType != null && !pointerType.isConst()) {
								return true;
							}
						}
					}

					return false;
				});
	}

	public static boolean pointeeIsLeftHandSideInModifyingBinaryExpression(ICPPASTName name, int dereferenceLevel) {
		return anyOfDescendingFrom(ICPPASTBinaryExpression.class, name,
				(ICPPASTBinaryExpression ancestor, IASTName reference) -> {
					ICPPASTBinaryExpression expression = ancestor;

					if (isDereferencedNTimes(reference, dereferenceLevel)) {

						if (isAnyOf(expression.getOperator(), MODIFYING_BINARY_OPERATORS)) {
							IASTIdExpression id = getDescendendOf(IASTIdExpression.class, expression.getOperand1());

							if (id != null) {
								return id.getName().getBinding().equals(name.getBinding());
							}
						}
					}

					return false;
				});
	}

	public static boolean pointeeIsLeftHandSideInModifyingUnaryExpression(ICPPASTName name, int dereferenceLevel) {
		return anyOfDescendingFrom(ICPPASTUnaryExpression.class, name,
				(ICPPASTUnaryExpression ancestor, IASTName reference) -> {
					ICPPASTUnaryExpression expression = ancestor;

					if (isDereferencedNTimes(reference, dereferenceLevel)) {
						while ((expression.getParent() instanceof IASTUnaryExpression)
								&& !isAnyOf(expression.getOperator(), MODIFYING_UNARY_OPERATORS)) {
							expression = (ICPPASTUnaryExpression) expression.getParent();
						}

						if (isAnyOf(expression.getOperator(), MODIFYING_UNARY_OPERATORS)) {
							IASTIdExpression id = getDescendendOf(IASTIdExpression.class, expression.getOperand());

							if (id != null) {
								return id.getName().getBinding().equals(name.getBinding());
							}
						}
					}

					return false;
				});
	}

	public static boolean pointeeIsPassedAsReferenceToNonConst(ICPPASTName name, int dereferenceLevel) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression ancestor, IASTName reference) -> {
					ICPPASTFunctionCallExpression expression = ancestor;

					if (isDescendendOf(ICPPASTFieldReference.class, reference)) {
						return false;
					}

					if (isDereferencedNTimes(reference, dereferenceLevel)) {

						List<Integer> indices = Node.getArgumentIndicesFor(expression.getArguments(), name, null);

						ICPPASTName functionName;
						if ((functionName = getNameForFunction(expression.getFunctionNameExpression())) == null) {
							return false;
						}

						IBinding binding = functionName.resolveBinding();
						ICPPFunction function = as(ICPPFunction.class, binding);

						if (function != null) {
							ICPPParameter[] parameters = function.getParameters();

							for (int index : indices) {
								ICPPParameter parameter = parameters[index];

								if (!(parameter.getType() instanceof ICPPReferenceType)) {
									continue;
								}

								ICPPReferenceType topLevelParameterType = (ICPPReferenceType) parameter.getType();
								IType referencedType = topLevelParameterType.getType();

								if (referencedType instanceof IPointerType) {
									return !((IPointerType) referencedType).isConst();
								} else if (referencedType instanceof IQualifierType) {
									return !((IQualifierType) referencedType).isConst();
								} else if (referencedType instanceof ICPPBasicType) {
									return true;
								} else if (referencedType instanceof ICPPClassType) {
									return true;
								}
							}
						}

					}

					return false;
				});
	}

	public static boolean pointeeIsUsedToBindReferenceToNonConst(ICPPASTName name, int dereferenceLevel) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer ancestor, IASTName reference) -> {
			IASTInitializer initializer = ancestor;

			ICPPASTDeclarator declarator;
			if ((declarator = getAncestorOf(ICPPASTDeclarator.class, initializer)) == null) {
				return false;
			}

			CPPVariable target = as(CPPVariable.class, declarator.getName().resolveBinding());
			IType targetType = target.getType();

			if (isReference(targetType)) {
				return !isConst(targetType, dereferenceLevel);
			}

			return false;
		});
	}

	public static boolean isPassedToFunctionTakingPointerToNonConst(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression ancestor, IASTName reference) -> {
					ICPPASTFunctionCallExpression expression = ancestor;

					if (isDescendendOf(ICPPASTFieldReference.class, reference)) {
						return false;
					}

					ArrayList<Integer> argumentIndexes = new ArrayList<>();
					IASTInitializerClause[] arguments = expression.getArguments();

					for (int i = 0; i < arguments.length; ++i) {
						IASTIdExpression idExpression = getDescendendOf(IASTIdExpression.class, arguments[i]);

						if (idExpression != null
								&& idExpression.getName().getBinding().equals(reference.getBinding())) {
							argumentIndexes.add(i);
						}
					}

					ICPPASTName functionName;
					if ((functionName = getNameForFunction(expression.getFunctionNameExpression())) == null) {
						return false;
					}

					ICPPFunction function = as(ICPPFunction.class, functionName.resolveBinding());

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : argumentIndexes) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof IPointerType)) {
								continue;
							}

							return !isConst(parameter.getType(), pointerLevel);
						}
					}

					return false;
				});
	}

	public static boolean isPassedToFunctionTakingReferenceToPointerToNonConst(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression call, IASTName reference) -> {
					if (!isDereferencedNTimes(reference, 0)) {
						return false;
					}

					ArrayList<Integer> argumentIndexes = new ArrayList<>();
					IASTInitializerClause[] arguments = call.getArguments();

					for (int i = 0; i < arguments.length; ++i) {
						IASTIdExpression idExpression = getDescendendOf(IASTIdExpression.class, arguments[i]);

						if (idExpression != null
								&& idExpression.getName().getBinding().equals(reference.getBinding())) {
							argumentIndexes.add(i);
						}
					}

					ICPPASTName functionName;
					if ((functionName = getNameForFunction(call.getFunctionNameExpression())) == null) {
						return false;
					}

					ICPPFunction function = as(ICPPFunction.class, functionName.resolveBinding());

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : argumentIndexes) {
							ICPPParameter parameter = parameters[index];
							IType parameterType = parameter.getType();

							return isReference(parameterType) && !isConst(parameterType, pointerLevel + 1);
						}
					}
					return false;
				});
	}

	public static boolean isAssignedToPointerToNonConst(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(ICPPASTBinaryExpression.class, name,
				(ICPPASTBinaryExpression expr, IASTName reference) -> {
					while (expr != null && expr.getOperator() != ICPPASTBinaryExpression.op_assign) {
						expr = getAncestorOf(ICPPASTBinaryExpression.class, expr.getParent());
					}

					if (expr == null) {
						return false;
					}

					ICPPASTName lhsName;
					ICPPASTName rhsName;
					if ((lhsName = getDescendendOf(ICPPASTName.class, expr.getOperand1())) == null
							|| (rhsName = getDescendendOf(ICPPASTName.class, expr.getOperand2())) == null) {
						return false;
					}

					ICPPVariable lhs;
					ICPPVariable rhs;
					if ((lhs = as(ICPPVariable.class, lhsName.resolveBinding())) == null
							|| (rhs = as(ICPPVariable.class, rhsName.resolveBinding())) == null) {
						return false;
					}

					if (!rhs.equals(name.resolveBinding())) {
						return false;
					}

					IType lhsType = lhs.getType();
					IType rhsType = rhs.getType();

					if (pointerLevels(lhsType) == pointerLevels(rhsType)) {
						return !isConst(lhsType, pointerLevel);
					}

					return !isConst(lhsType, pointerLevel + 1);
				});
	}

	public static boolean isUsedToInitializePointerToNonConst(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer init, IASTName reference) -> {
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, init);
			ICPPVariable lhs;

			if(declarator == null) {
				return false;
			}

			if ((lhs = as(ICPPVariable.class, declarator.getName().resolveBinding())) == null) {
				return false;
			}

			if (!areSameTypeIgnoringConst(lhs.getType(), ((ICPPVariable) name.resolveBinding()).getType())) {
				return false;
			}

			return !isConst(lhs.getType(), pointerLevel);
		});
	}

	public static boolean isUsedToBindReferenceToPointerToNonConst(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer init, IASTName reference) -> {
			if (!isDereferencedNTimes(reference, 0)) {
				return false;
			}

			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, init);
			if(declarator == null) {
				return false;
			}
			ICPPVariable lhs;

			if ((lhs = as(ICPPVariable.class, declarator.getName().resolveBinding())) == null) {
				return false;
			}

			return isReference(lhs.getType()) && !isConst(lhs.getType(), pointerLevel + 1);
		});
	}

	public static boolean isInitializedUsingLessConstPointer(ICPPASTName name, int pointerLevel) {
		ICPPASTDeclarator decl;
		if ((decl = getAncestorOf(ICPPASTDeclarator.class, name)) == null) {
			return false;
		}

		IASTInitializer init;
		if ((init = decl.getInitializer()) == null) {
			return false;
		}

		IASTNode[] children;
		if ((children = init.getChildren()).length == 0) {
			return false;
		}

		ICPPASTName other;
		if ((other = getDescendendOf(ICPPASTName.class, children[0])) == null) {
			return false;
		}

		ICPPVariable otherPtr;
		if ((otherPtr = as(ICPPVariable.class, other.resolveBinding())) == null) {
			return false;
		}

		IBinding ptrBinding = name.resolveBinding();
		IType ptrType = ptrBinding instanceof ICPPVariable ? ((ICPPVariable) ptrBinding).getType()
				: ((ICPPParameter) ptrBinding).getType();

		if (!isConst(otherPtr.getType(), pointerLevel) && pointerLevel > 1) {
			return !isConst(ptrType, pointerLevel - 1);
		}

		return false;
	}

	public static boolean nonConstMemberAccessedOnPointee(ICPPASTName name, int pointerLevel) {
		return anyOfDescendingFrom(ICPPASTFieldReference.class, name,
				(ICPPASTFieldReference expression, IASTName reference) -> {
					ICPPASTName owner = getResultingName(expression.getFieldOwner());

					if(owner != null && owner.equals(reference)) {
						IASTName field = expression.getFieldName();

						if(field != null) {
							ICPPMember member = as(ICPPMember.class, field.resolveBinding());

							try {
								return !isConst(member.getType(), 0);
							} catch (Exception e) {
							}
						}
					}

					return false;
				});
	}

}
