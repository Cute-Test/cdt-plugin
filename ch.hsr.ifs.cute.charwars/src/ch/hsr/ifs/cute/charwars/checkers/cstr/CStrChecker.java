package ch.hsr.ifs.cute.charwars.checkers.cstr;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.checkers.BaseChecker;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.constants.StdString;

public class CStrChecker extends BaseChecker {
	public CStrChecker() {
		this.astVisitor = new CStrVisitor();
	}
	
	private class CStrVisitor extends ASTVisitor {
		private final OverloadChecker overloadChecker = new OverloadChecker();
		
		public CStrVisitor() {
			shouldVisitExpressions = true;
		}
		
		@Override
		public int visit(IASTExpression expression) {
			if(ASTAnalyzer.isCallToMemberFunction(expression, StdString.C_STR)) {
				IASTFunctionCallExpression cStrCall = (IASTFunctionCallExpression)expression;
				IASTNode parent = cStrCall.getParent();
				IASTName name = null;
				int strArgIndex = -1;
				
				if(parent instanceof IASTFunctionCallExpression) {
					IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression)parent;
					name = getFunctionName(functionCallExpression);
					strArgIndex = Arrays.asList(functionCallExpression.getArguments()).indexOf(cStrCall);
				}
				else if(parent instanceof ICPPASTBinaryExpression) {
					ICPPASTBinaryExpression binaryExpression = (ICPPASTBinaryExpression)parent;
					if(binaryExpression.getImplicitNames().length == 0) {
						return PROCESS_CONTINUE;
					}
					name = binaryExpression.getImplicitNames()[0];
					strArgIndex = binaryExpression.getOperand1() == cStrCall ? 0 : 1;
				}
				else if(parent.getParent() instanceof ICPPASTDeclarator && parent.getParent() instanceof IASTImplicitNameOwner) {
					IASTImplicitNameOwner implicitNameOwner = (IASTImplicitNameOwner)parent.getParent();
					if(implicitNameOwner.getImplicitNames().length == 0) {
						return PROCESS_CONTINUE;
					}
					name = implicitNameOwner.getImplicitNames()[0];
					strArgIndex = 0;
				}
				
				if(name != null) {
					ICPPFunction[] validOverloads = overloadChecker.getValidOverloads(name, strArgIndex);
					if(validOverloads.length > 0) {
	//					for(ICPPFunction overload : validOverloads) {
	//						System.out.println(overload);
	//					}
	//					System.out.println(validOverloads.length);
	//					System.out.println("");
						ICPPFunction firstValidOverload = validOverloads[0];
						reportProblemForNode(ProblemIDs.C_STR_PROBLEM, cStrCall, getSignature(firstValidOverload));
					}
				}
			}
			return PROCESS_CONTINUE;
		}
		
		private String getSignature(ICPPFunction function) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(ASTTypeUtil.getType(function.getType().getReturnType()));
			buffer.append(" ");
			buffer.append(function.getName());
			buffer.append("(");
			
			ICPPParameter parameters[] = function.getParameters();
			for(ICPPParameter parameter : parameters) {
				buffer.append(ASTTypeUtil.getType(parameter.getType()));
				buffer.append(" ");
				buffer.append(parameter.getName());
				
				if(parameter != parameters[parameters.length-1]) {
					buffer.append(", ");
				}
			}
			
			buffer.append(")");
			return buffer.toString();
		}
		
		private IASTName getFunctionName(IASTFunctionCallExpression functionCall) {
			IASTName functionName = null;
			IASTExpression functionNameExpression = functionCall.getFunctionNameExpression();
			
			if(functionNameExpression instanceof IASTIdExpression) {
				functionName = ((IASTIdExpression)functionNameExpression).getName();
			}
			else if(functionNameExpression instanceof IASTFieldReference) {
				functionName = ((IASTFieldReference)functionNameExpression).getFieldName();
			}
			
			return functionName;
		}
	}
}