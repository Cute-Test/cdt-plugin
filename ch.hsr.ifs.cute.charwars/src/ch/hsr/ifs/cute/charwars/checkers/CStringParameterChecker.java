package ch.hsr.ifs.cute.charwars.checkers;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.FunctionBindingAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

public class CStringParameterChecker extends BaseChecker {
	public CStringParameterChecker() {
		this.astVisitor = new CStringParameterVisitor();
	}
	
	private class CStringParameterVisitor extends ASTVisitor {
		public CStringParameterVisitor() {
			shouldVisitParameterDeclarations = true;
		}
		
		@Override
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			IASTDeclarator declarator = parameterDeclaration.getDeclarator();
			if(ASTAnalyzer.isFunctionDefinitionParameterDeclaration(parameterDeclaration) && ASTAnalyzer.isConstCStringParameter(declarator)) {
				if(!isStdStringOverloadAvailable((IASTFunctionDefinition)parameterDeclaration.getParent().getParent(), parameterDeclaration)) {
					reportProblemForDeclarator(ProblemIDs.C_STRING_PARAMETER_PROBLEM, declarator);
				}
			}
			return PROCESS_CONTINUE;
		}
		
		private boolean isStdStringOverloadAvailable(IASTFunctionDefinition functionDefinition, IASTParameterDeclaration cStrParameter) {
			try {
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)functionDefinition.getDeclarator();
				int strParameterIndex = Arrays.asList(functionDeclarator.getParameters()).indexOf(cStrParameter);
				IASTName functionName = functionDeclarator.getName();
				
				IIndex index = functionName.getTranslationUnit().getIndex();
				ICPPFunction originalOverload = (ICPPFunction)index.adaptBinding(functionName.resolveBinding());
				IIndexBinding bindings[] = IndexFinder.findBindings(functionName, false);
				for(IIndexBinding binding : bindings) {
					if(binding instanceof ICPPFunction) {
						ICPPFunction possibleOverload = (ICPPFunction)binding;
						if(FunctionBindingAnalyzer.isValidOverload(originalOverload, possibleOverload, strParameterIndex)) {
							return true;
						}
					}
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
