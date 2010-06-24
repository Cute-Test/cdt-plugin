package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

import ch.hsr.ifs.cute.ui.ASTUtil;

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
		if(ASTUtil.isTestFunction(declaration)) {
			testFunctions.add(declaration);
		}
		return super.visit(declaration);
	}	

}