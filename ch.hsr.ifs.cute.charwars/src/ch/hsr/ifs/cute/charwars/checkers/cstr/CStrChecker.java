package ch.hsr.ifs.cute.charwars.checkers.cstr;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.FunctionBindingAnalyzer;
import ch.hsr.ifs.cute.charwars.checkers.BaseChecker;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;

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
			if(ASTAnalyzer.isConversionToCharPointer(expression, true)) {
				IASTFunctionCallExpression cStrCall = (IASTFunctionCallExpression)expression;
				IASTNode parent = cStrCall.getParent();
				IASTName name = null;
				int strArgIndex = -1;
				
				if(parent instanceof ICPPASTBinaryExpression) {
					ICPPASTBinaryExpression binaryExpression = (ICPPASTBinaryExpression)parent;
					IASTImplicitName implicitNames[] = binaryExpression.getImplicitNames();
					if(implicitNames.length == 0) {
						return PROCESS_CONTINUE;
					}
					name = implicitNames[0];
					strArgIndex = BEAnalyzer.isOp1(cStrCall) ? 0 : 1;
				}
				else {
					name = FunctionBindingAnalyzer.getFunctionName(parent);
					strArgIndex = FunctionBindingAnalyzer.getArgIndex(parent, cStrCall);
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
	}
}