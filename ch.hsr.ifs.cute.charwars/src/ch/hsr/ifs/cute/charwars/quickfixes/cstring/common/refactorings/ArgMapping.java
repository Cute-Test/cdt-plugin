package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;

public class ArgMapping {
	private Arg[] args;
	
	public enum Arg {
		ARG_0,
		ARG_1,
		ARG_2,
		OFF_0,
		ZERO,
		NPOS,
		BEGIN,
		END
	}
	
	public ArgMapping(Arg... args) {
		this.args = args;
	}
	
	public IASTNode[] getOutArguments(IASTInitializerClause inArgs[], IASTIdExpression idExpression, Context context) {
		List<IASTNode> outArgs = new ArrayList<IASTNode>();
		
		for(Arg arg : args) {
			IASTNode outArg = getOutArgument(arg, inArgs, idExpression, context);
			outArgs.add(outArg);
		}
		
		return outArgs.toArray(new IASTNode[]{});
	}
	
	private IASTNode getOutArgument(Arg arg, IASTInitializerClause inArgs[], IASTIdExpression idExpression, Context context) {
		switch(arg) {
		case ARG_0:
			return ASTAnalyzer.extractStdStringArg(inArgs[0]);
		case ARG_1:
			return ASTAnalyzer.extractStdStringArg(inArgs[1]);
		case ARG_2:
			return ASTAnalyzer.extractStdStringArg(inArgs[2]);
		case OFF_0:
			return context.getOffset(idExpression);
		case ZERO:
			return ExtendedNodeFactory.newIntegerLiteral(0);
		case NPOS:
			return ExtendedNodeFactory.newNposExpression(context.getStringType());
		case BEGIN:
			return ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.BEGIN);
		case END:
			IASTExpression arg2 = (IASTExpression)inArgs[2];
			if(FunctionAnalyzer.isCallToMemberFunction(arg2, Function.SIZE)) {
				return ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.END);
			}
			else {
				IASTExpression beginCall = ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.BEGIN);
				return ExtendedNodeFactory.newPlusExpression(beginCall, arg2);
			}
		default:
			return null;
		}	
	}
}
