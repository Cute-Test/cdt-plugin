package ch.hsr.ifs.cute.constificator.core.deciders.util;

import static ch.hsr.ifs.cute.constificator.core.deciders.util.Constants.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Node.*;
import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Arrays.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

import ch.hsr.ifs.cute.constificator.core.util.ast.Node;
import ch.hsr.ifs.cute.constificator.core.util.trait.Types;

@SuppressWarnings("restriction")
public class FunctionBodyVisitor extends ASTVisitor {

	private boolean modifiesDataMember = false;
	private final ICPPField[] memberVariables;
	private final ICPPMethod[] memberFunctions;

	private List<ICPPASTName> getMemberUsesIn(IASTStatement line) {
		List<ICPPASTName> names = getDescendendsOf(ICPPASTName.class, line);

		ICPPASTName current = null;
		for (Iterator<ICPPASTName> it = names.iterator(); it.hasNext();) {
			current = it.next();
			if (!isAnyOf(current.resolveBinding(), memberVariables)) {
				it.remove();
			}
		}

		return names;
	}

	private List<ICPPASTName> getMemberFunctionsUsedOn(IASTStatement line) {
		List<ICPPASTName> names = getDescendendsOf(ICPPASTName.class, line);

		Iterator<ICPPASTName> it = names.iterator();
		while (it.hasNext()) {
			if (!isAnyOf(it.next().resolveBinding(), memberFunctions)) {
				it.remove();
			}
		}

		return names;
	}

	public FunctionBodyVisitor(ICPPField[] memberVariables, ICPPMethod[] memberFunctions) {
		this.memberVariables = memberVariables;
		this.memberFunctions = memberFunctions;
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof ICPPASTCompoundStatement) {
			for (IASTStatement line : ((ICPPASTCompoundStatement) statement).getStatements()) {
				if (line != null) {
					process(line);
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	private void process(IASTStatement line) {
		List<ICPPASTExpression> expressions = getDescendendsOf(ICPPASTExpression.class, line);

		for (ICPPASTExpression expression : expressions) {

			List<ICPPASTName> memberUses = getMemberUsesIn(line);
			for (ICPPASTName use : memberUses) {

				ICPPVariable useBinding = as(ICPPVariable.class, use.resolveBinding());
				if (Types.pointerLevels(useBinding.getType()) > 0) {
					if (!isDereferencedNTimes(use, 0)) {
						continue;
					}
				}

				if (expression instanceof ICPPASTBinaryExpression) {
					handleBinary((ICPPASTBinaryExpression) expression, use);
				} else if (expression instanceof ICPPASTUnaryExpression) {
					handleUnary((ICPPASTUnaryExpression) expression, use);
				} else if (expression instanceof ICPPASTFunctionCallExpression) {
					handleFunctionCall((ICPPASTFunctionCallExpression) expression, use);
				} else if (expression instanceof ICPPASTFieldReference) {
					handleFieldReference((ICPPASTFieldReference) expression, use);
				}

				if (modifiesDataMember) {
					break;
				}
			}

			if (expression instanceof ICPPASTFunctionCallExpression) {
				memberUses = getMemberFunctionsUsedOn(line);
				for (ICPPASTName use : memberUses) {
					ICPPMethod useBinding = as(ICPPMethod.class, use.resolveBinding());
					modifiesDataMember |= !isConst(useBinding.getType(), 0);
				}

			}
		}

		if(line instanceof IASTReturnStatement) {
			handleReturnStatement((IASTReturnStatement) line);
		}
	}

	private void handleReturnStatement(IASTReturnStatement expression) {
		ICPPASTName name = getResultingName((ICPPASTExpression) expression.getReturnValue());

		if (name != null && isAnyOf(name.resolveBinding(), memberVariables)) {
			ICPPASTFunctionDefinition functionDefinition;

			if ((functionDefinition = getAncestorOf(ICPPASTFunctionDefinition.class, name)) != null) {
				ICPPMethod function;
				if ((function = as(ICPPMethod.class,
						functionDefinition.getDeclarator().getName().resolveBinding())) != null) {
					ICPPMember member = as(ICPPMember.class, name.getBinding());
					IType returnType = function.getType().getReturnType();
					int pointerLevels = pointerLevels(returnType);

					if (isReference(returnType)) {
						modifiesDataMember |= !isConst(returnType, 1);
					} else if (pointerLevels > 0) {
						try {
							IType memberType = member.getType();
							if (member != null && pointerLevels > pointerLevels(memberType)) {
								modifiesDataMember |= !isConst(returnType, pointerLevels(memberType) + 1);
							}
						} catch (DOMException e) {
						}
					}
				}

			}
		}
	}

	private void handleFieldReference(ICPPASTFieldReference expression, ICPPASTName use) {

		IASTName fieldName = expression.getFieldName();
		ICPPASTName resultingName = getResultingName(expression.getFieldOwner());

		if (resultingName.equals(use)) {
			IBinding fieldBinding = fieldName.getBinding();

			if (fieldBinding instanceof ICPPMember) {
				try {
					modifiesDataMember |= !isConst(((ICPPMember) fieldBinding).getType(), 0);
				} catch (DOMException e) {
				}
			}
		}

	}

	private void handleBinary(ICPPASTBinaryExpression binary, ICPPASTName name) {
		ICPPASTName lhs = getDescendendOf(ICPPASTName.class, binary.getOperand1());
		if (lhs != null && lhs.equals(name)) {
			modifiesDataMember |= isAnyOf(binary.getOperator(), MODIFYING_BINARY_OPERATORS);
		}
	}

	private void handleUnary(ICPPASTUnaryExpression unary, ICPPASTName name) {
		ICPPASTName operand = getDescendendOf(ICPPASTName.class, unary.getOperand());
		if (operand != null && operand.equals(name)) {
			modifiesDataMember |= isAnyOf(unary.getOperator(), MODIFYING_UNARY_OPERATORS);
		}
	}

	private void handleFunctionCall(ICPPASTFunctionCallExpression expression, ICPPASTName name) {
		ICPPASTName functionName = Node.getNameForFunction(expression.getFunctionNameExpression());
		ICPPFunction function;
		if ((function = as(ICPPFunction.class, functionName.resolveBinding())) == null) {
			return;
		}

		if (function instanceof ICPPUnknownBinding) {
			modifiesDataMember = true;
			return;
		}

		if (function instanceof ICPPMethod && !isConst(function.getType(), 0)) {
			modifiesDataMember = true;
		}

		ICPPParameter[] parameters = function.getParameters();
		IASTInitializerClause[] arguments = expression.getArguments();

		if (parameters.length != arguments.length) {
			return;
		}

		List<Integer> indices = new ArrayList<>();

		for (int i = 0; i < arguments.length; ++i) {
			IASTInitializerClause arg = arguments[i];
			IASTIdExpression id;
			if ((id = getDescendendOf(IASTIdExpression.class, arg)) == null) {
				continue;
			}

			if (!(id.getName().resolveBinding() instanceof ICPPField)) {
				continue;
			} else {
				indices.add(i);
			}
		}

		for (int index : indices) {
			IType type = parameters[index].getType();

			if (isReference(type) && !isConst(type, 1)) {
				modifiesDataMember = true;
			}
		}
	}

	public boolean modifiesDataMember() {
		return modifiesDataMember;
	}

}
