package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.FunctionBindingAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.utils.analyzers.BEAnalyzer;

public class CStrProblemGenerator {
	public static List<ProblemReport> generate(IFile file, IASTExpression expression) {
		List<ProblemReport> problemReports = new ArrayList<ProblemReport>();
		if(ASTAnalyzer.isConversionToCharPointer(expression, true)) {
			IASTFunctionCallExpression cStrCall = (IASTFunctionCallExpression)expression;
			IASTNode parent = cStrCall.getParent();
			IASTName name = null;
			int strArgIndex = -1;
			
			if(parent instanceof ICPPASTBinaryExpression) {
				ICPPASTBinaryExpression binaryExpression = (ICPPASTBinaryExpression)parent;
				IASTImplicitName implicitNames[] = binaryExpression.getImplicitNames();
				if(implicitNames.length == 0) {
					return problemReports;
				}
				name = implicitNames[0];
				strArgIndex = BEAnalyzer.isOp1(cStrCall) ? 0 : 1;
			}
			else {
				name = FunctionBindingAnalyzer.getFunctionName(parent);
				strArgIndex = FunctionBindingAnalyzer.getArgIndex(parent, cStrCall);
			}
			
			if(name != null) {
				OverloadChecker overloadChecker = new OverloadChecker();
				ICPPFunction[] validOverloads = overloadChecker.getValidOverloads(name, strArgIndex);
				if(validOverloads.length > 0) {
					ICPPFunction firstValidOverload = validOverloads[0];
					String signature = getSignature(firstValidOverload);
					problemReports.add(new ProblemReport(file, ProblemIDs.C_STR_PROBLEM, cStrCall, signature));
				}
			}
		}
		return problemReports;
	}
	
	private static String getSignature(ICPPFunction function) {
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
