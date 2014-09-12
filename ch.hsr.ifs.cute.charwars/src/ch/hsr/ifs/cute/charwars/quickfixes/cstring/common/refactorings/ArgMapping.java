package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;

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
	
	public IASTNode[] getOutArguments(IASTInitializerClause inArguments[], IASTIdExpression idExpression, Context context) {
		List<IASTNode> outArguments = new ArrayList<IASTNode>();
		
		for(Arg arg : args) {
			switch(arg) {
			case ARG_0:
				outArguments.add(ASTAnalyzer.extractStdStringArg(inArguments[0]));
				break;
			case ARG_1:
				outArguments.add(ASTAnalyzer.extractStdStringArg(inArguments[1]));
				break;
			case ARG_2:
				outArguments.add(ASTAnalyzer.extractStdStringArg(inArguments[2]));
				break;
			case OFF_0:
				outArguments.add(ASTAnalyzer.getOffset(idExpression, context));
				break;
			case ZERO:
				outArguments.add(ExtendedNodeFactory.newIntegerLiteral(0));
				break;
			case NPOS:
				outArguments.add(ExtendedNodeFactory.newNposExpression());
				break;
			case BEGIN:
				outArguments.add(ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.BEGIN));
				break;
			case END:
				IASTExpression arg2 = (IASTExpression)inArguments[2];
				if(ASTAnalyzer.isCallToMemberFunction(arg2, Function.SIZE)) {
					outArguments.add(ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.END));
				}
				else {
					IASTExpression beginCall = ExtendedNodeFactory.newMemberFunctionCallExpression(idExpression.getName(), StdString.BEGIN);
					outArguments.add(ExtendedNodeFactory.newPlusExpression(beginCall, arg2));
				}
				break;
			}	
		}
		
		return outArguments.toArray(new IASTNode[]{});
	}
}
