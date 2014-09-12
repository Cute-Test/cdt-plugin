package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class FunctionRefactoring extends Refactoring {
	private Function inFunction;
	private Function outFunction;
	private ArgMapping argMapping;
	
	public FunctionRefactoring(Function inFunction, Function outFunction, ArgMapping argMapping, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		this.argMapping = argMapping;
		this.contextStates = new HashSet<ContextState>();
		for(ContextState contextState : contextStates) {
			this.contextStates.add(contextState);
		}
	}

	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		String inFunctionName = inFunction.getName();
		if(canHandleOffsets() && (ASTAnalyzer.isOffset(idExpression, context) || ASTAnalyzer.hasOffset(idExpression, inFunctionName))) {
			if(ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, inFunctionName) || 
			   ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
				IASTNode nodeToReplace = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunctionName);
				isApplicable = true;
				config.put(NODE_TO_REPLACE, nodeToReplace);
			}
		}
		else if(!context.isOffset(idExpression)) {
			if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName) ||
			   ASTAnalyzer.isFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
				isApplicable = true;
				config.put(NODE_TO_REPLACE, idExpression.getParent());
			}
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTName stringName = ExtendedNodeFactory.newName(context.getStringVarName());
		String outFunctionName = outFunction.getName();
		boolean isMemberFunction = outFunction.isMemberFunction();
		IASTNode nodeToReplace = (IASTNode)config.get(NODE_TO_REPLACE);
		IASTFunctionCallExpression inFunctionCall = (IASTFunctionCallExpression)nodeToReplace;
		IASTNode adaptedArguments[] = argMapping.getOutArguments(inFunctionCall.getArguments(), idExpression, context);
		
		if(isMemberFunction) {
			IASTFunctionCallExpression memberFunctionCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, outFunctionName, adaptedArguments);	
			
			//special case for strlen() / wcslen()
			if(outFunctionName.equals(StdString.SIZE) && (idExpression.getParent() != nodeToReplace || context.isOffset(idExpression))) {
				IASTNode offset = ASTAnalyzer.getOffset(idExpression, context);
				IASTBinaryExpression minusExpression = ExtendedNodeFactory.newMinusExpression(memberFunctionCall, (IASTExpression)offset);
				return ExtendedNodeFactory.newBracketedExpression(minusExpression);
			}
				
			return memberFunctionCall;	
		}
		else {
			return ExtendedNodeFactory.newFunctionCallExpression(outFunctionName, adaptedArguments);
		}
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setStatementHasChanged(true);
		changeDescription.addHeaderToInclude(outFunction.getHeader());
	}
}