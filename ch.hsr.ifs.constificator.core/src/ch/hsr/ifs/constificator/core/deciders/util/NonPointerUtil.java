package ch.hsr.ifs.constificator.core.deciders.util;

import static ch.hsr.ifs.constificator.core.deciders.util.Constants.*;
import static ch.hsr.ifs.constificator.core.util.ast.Node.*;
import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.constificator.core.util.type.Arrays.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

import ch.hsr.ifs.constificator.core.util.ast.Node;
import ch.hsr.ifs.constificator.core.util.trait.Types;
import ch.hsr.ifs.constificator.core.util.type.Pair;

@SuppressWarnings("restriction")
public class NonPointerUtil {

	public static boolean isLeftHandSideInModifyingBinaryExpression(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTBinaryExpression.class, name,
				(ICPPASTBinaryExpression binary, IASTName reference) -> {
					if (!isDereferencedNTimes(reference, 0)) {
						return false;
					}

					IASTName lhs;
					if ((lhs = getDescendendOf(ICPPASTName.class, binary.getOperand1())) == null) {
						return false;
					}
					return lhs.resolveBinding().equals(name.resolveBinding())
							&& isAnyOf(binary.getOperator(), MODIFYING_BINARY_OPERATORS);
				});
	}

	public static boolean isOperandInModifyingUnaryExpression(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTUnaryExpression.class, name,
				(ICPPASTUnaryExpression unary, IASTName reference) -> {
					if (!isDereferencedNTimes(reference, 0)) {
						return false;
					}

					IASTName lhs;
					if ((lhs = getDescendendOf(ICPPASTName.class, unary.getOperand())) == null) {
						return false;
					}
					return lhs.resolveBinding().equals(name.resolveBinding())
							&& isAnyOf(unary.getOperator(), MODIFYING_UNARY_OPERATORS);
				});
	}

	public static Pair<Boolean, Pair<IASTName, Integer>> isPassedAsLValueReferenceToNonConst(ICPPASTName name) {
		Pair<Boolean, Pair<IASTName, Integer>> result = new Pair<>(false, new Pair<>(null, -1));

		Boolean violates = anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression expression, IASTName reference) -> {
					List<Integer> indices = getArgumentIndicesFor(expression.getArguments(), name, (n) -> {
						return isDereferencedNTimes(n, 0);
					});

					if(indices.size() == 0) {
						return false;
					}

					ICPPASTName functionName;
					if ((functionName = Node.getNameForFunction(expression.getFunctionNameExpression())) == null) {
						return true;
					}

					ICPPFunction function;
					if((function = as(ICPPFunction.class, functionName.resolveBinding())) == null) {
						return true;
					}

					if (function instanceof ICPPDeferredFunction) {
						return true;
					}

					if(function.takesVarArgs()) {
						return true;
					}

					if(function.getParameters().length <= indices.get(indices.size() -1)) {
						return true;
					}

					if(function instanceof ICPPUnknownBinding) {
						return true;
					}

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : indices) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof ICPPReferenceType)
									|| ((ICPPReferenceType) parameter.getType()).isRValueReference()) {
								continue;
							}

							ICPPReferenceType parameterType = (ICPPReferenceType) parameter.getType();

							if (!Types.isConst(parameterType, 1)) {
								result.second().first(functionName);
								result.second().second(index);
								return true;
							}
						}
					}

					return false;
				});

		result.first(violates);
		return result;
	}

	public static boolean isUsedToBindLValueReferenceToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer initializer, IASTName reference) -> {
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, initializer);

			if (declarator == null) {
				return false;
			}

			IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();

			if (pointerOperators == null || pointerOperators.length != 1) {
				return false;
			}

			ICPPASTReferenceOperator referenceOperator = as(ICPPASTReferenceOperator.class, pointerOperators[0]);

			if (referenceOperator != null && referenceOperator.isRValueReference()) {
				return false;
			}

			IASTSimpleDeclaration declaration = getAncestorOf(IASTSimpleDeclaration.class, declarator);

			return !declaration.getDeclSpecifier().isConst();
		});
	}

	public static Pair<Boolean, Pair<IASTName, Integer>> addressIsPassedAsPointerToNonConst(ICPPASTName name) {
		Pair<Boolean, Pair<IASTName, Integer>> result = new Pair<>(false, new Pair<>(null, -1));

		Boolean violates = anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression expression, IASTName reference) -> {

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

					IASTName functionASTName;
					IASTExpression functionName = expression.getFunctionNameExpression();
					if (functionName instanceof IASTIdExpression) {
						functionASTName = ((IASTIdExpression) functionName).getName();
					} else if (functionName instanceof ICPPASTFieldReference) {
						functionASTName = ((ICPPASTFieldReference) functionName).getFieldName();
					} else {
						return false;
					}
					ICPPFunction function = as(ICPPFunction.class, functionASTName.resolveBinding());

					if (function != null) {
						ICPPParameter[] parameters = function.getParameters();

						for (int index : argumentIndexes) {
							ICPPParameter parameter = parameters[index];

							if (!(parameter.getType() instanceof IPointerType)) {
								continue;
							}

							IPointerType parameterType = (IPointerType) parameter.getType();
							IQualifierType qualifiedType = as(IQualifierType.class, parameterType.getType());

							if (qualifiedType == null) {
								result.second().first(functionASTName);
								result.second().second(index);
								return true;
							}
						}
					}

					return false;
				});

		result.first(violates);
		return result;
	}

	public static boolean addressIsAssignedToPointerToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer initializer, IASTName reference) -> {
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, initializer);

			if (declarator == null) {
				return false;
			}

			IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();

			if (pointerOperators == null || pointerOperators.length != 1) {
				return false;
			}

			IASTPointer pointerOperator = as(IASTPointer.class, pointerOperators[0]);

			if (pointerOperator == null) {
				return false;
			}

			IASTSimpleDeclaration declaration = getAncestorOf(IASTSimpleDeclaration.class, declarator);

			return !declaration.getDeclSpecifier().isConst();
		});
	}

	public static boolean addressIsPassedAsReferenceToPointerToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFunctionCallExpression.class, name,
				(ICPPASTFunctionCallExpression expression, IASTName reference) -> {
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

					IBinding binding;
					IASTExpression functionName = expression.getFunctionNameExpression();
					if (functionName instanceof IASTIdExpression) {
						binding = ((IASTIdExpression) functionName).getName().resolveBinding();
					} else if (functionName instanceof ICPPASTFieldReference) {
						binding = ((ICPPASTFieldReference) functionName).getFieldName().resolveBinding();
					} else {
						return false;
					}
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
							IQualifierType qualifiedType = as(IQualifierType.class, referencedType.getType());

							if (qualifiedType == null) {
								return true;
							}
						}
					}

					return false;
				});
	}

	public static boolean addressIsUsedToBindReferenceToPointerToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(IASTInitializer.class, name, (IASTInitializer initializer, IASTName reference) -> {
			ICPPASTDeclarator declarator = getAncestorOf(ICPPASTDeclarator.class, initializer);
			IASTSimpleDeclaration declaration = getAncestorOf(IASTSimpleDeclaration.class, declarator);
			ICPPASTUnaryExpression unary = getAncestorOf(ICPPASTUnaryExpression.class, reference);

			return unary != null && unary.getOperator() == IASTUnaryExpression.op_amper && declarator != null
					&& declarator.getPointerOperators().length == 2 && declaresLValueReferenceToPointer(declarator)
					&& !declaration.getDeclSpecifier().isConst();
		});
	}

	public static boolean nonConstMemberFunctionCalledOn(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFieldReference.class, name,
				(ICPPASTFieldReference expression, IASTName reference) -> {
					if (!(expression.getParent() instanceof IASTFunctionCallExpression)) {
						return false;
					}

					IBinding fieldBinding = expression.getFieldName().getBinding();
					ICPPMethod memberFunction = as(ICPPMethod.class, fieldBinding);

					if (memberFunction == null) {
						return false;
					}

					return !memberFunction.getType().isConst();
				});
	}

	public static boolean nonConstMemberAccessed(ICPPASTName name) {
		return anyOfDescendingFrom(ICPPASTFieldReference.class, name,
				(ICPPASTFieldReference expression, IASTName reference) -> {
					ICPPField field;
					if ((field = as(ICPPField.class, expression.getFieldName().resolveBinding())) == null) {
						return false;
					}

					return !isConst(field.getType(), 0);
				});
	}

	public static boolean isReturnedAsReferenceToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(IASTReturnStatement.class, name,
				(IASTReturnStatement statement, IASTName reference) -> {
					ICPPASTExpression returnValue = as(ICPPASTExpression.class, statement.getReturnValue());
					ICPPASTName resultingName = getResultingName(returnValue);

					if(resultingName != null) {
						if(!resultingName.resolveBinding().equals(name.resolveBinding())) {
							return false;
						}

						ICPPASTFunctionDefinition functionDefinition;
						if((functionDefinition = getAncestorOf(ICPPASTFunctionDefinition.class, statement)) == null) {
							return false;
						}

						ICPPFunction function;
						if((function = as(ICPPFunction.class, functionDefinition.getDeclarator().getName().resolveBinding())) == null) {
							return false;
						}

						return isReference(function.getType().getReturnType()) && !isConst(function.getType().getReturnType(), 1);
					}

					return false;
				});
	}

	public static boolean isReturnedAsPointerToNonConst(ICPPASTName name) {
		return anyOfDescendingFrom(IASTReturnStatement.class, name,
				(IASTReturnStatement statement, IASTName reference) -> {
					ICPPASTExpression returnValue = as(ICPPASTExpression.class, statement.getReturnValue());
					ICPPASTName resultingName = getResultingName(returnValue);

					if(resultingName != null) {
						if(!resultingName.resolveBinding().equals(name.resolveBinding()) || !isUsedToTakeAddressOf(resultingName)) {
							return false;
						}

						ICPPASTFunctionDefinition functionDefinition;
						if((functionDefinition = getAncestorOf(ICPPASTFunctionDefinition.class, statement)) == null) {
							return false;
						}

						ICPPFunction function;
						if((function = as(ICPPFunction.class, functionDefinition.getDeclarator().getName().resolveBinding())) == null) {
							return false;
						}

						return !isConst(function.getType().getReturnType(), 1);
					}

					return false;
				});
	}

}
