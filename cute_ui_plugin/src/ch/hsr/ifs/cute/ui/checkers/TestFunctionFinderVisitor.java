package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;

class TestFunctionFinderVisitor extends ASTVisitor{
	/**
	 * 
	 */
	private final List<IASTDeclaration> testFunctions;

	/**
	 * @param testFunctions
	 */
	TestFunctionFinderVisitor() {
		testFunctions = new ArrayList<IASTDeclaration>();
	}

	{
		shouldVisitDeclarations = true;
	}
	
	public List<IASTDeclaration> getTestFunctions(){
		return testFunctions;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if(isTestFunction(declaration)) {
			testFunctions.add(declaration);
		}
		return super.visit(declaration);
	}

	private boolean isTestFunction(IASTDeclaration declaration) {
		if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) declaration;
			if(hasNoParameters(funcDef)) {
				if(containsAssert(funcDef)) {
					return true;
				}
			}
		}
		return false;				
	}

	private boolean containsAssert(IASTFunctionDefinition funcDef) {
		AssertStatementCheckVisitor checker = new AssertStatementCheckVisitor();
		funcDef.getBody().accept(checker);
		return checker.hasAssertStmt;
	}

	private boolean hasNoParameters(IASTFunctionDefinition funcDef) {
		IASTFunctionDeclarator declarator = funcDef.getDeclarator();
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator stdFuncDecl = (IASTStandardFunctionDeclarator) declarator;
			if(stdFuncDecl.getParameters() == null || stdFuncDecl.getParameters().length == 0) {
				return true;
			}
		}
		return false;
	}
	

}