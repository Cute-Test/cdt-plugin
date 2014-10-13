package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;

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
			if(ASTAnalyzer.isFunctionDefinitionParameterDeclaration(parameterDeclaration) && DeclaratorTypeAnalyzer.hasCStringType(declarator, true)) {
				if(!isStdStringOverloadAvailable(parameterDeclaration)) {
					reportProblemForDeclarator(ProblemIDs.C_STRING_PARAMETER_PROBLEM, declarator);
				}
			}
			return PROCESS_CONTINUE;
		}
		
		private boolean isStdStringOverloadAvailable(IASTParameterDeclaration cStrParameter) {
			try {
				int strParameterIndex = FunctionAnalyzer.getParameterIndex(cStrParameter);
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)cStrParameter.getParent();
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
