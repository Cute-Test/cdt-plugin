package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

public class SuitePushBackFinder extends ASTVisitor {
	private IASTName suiteDeclName = null;

	
	{
		shouldVisitStatements = true;
	}

	//save the name of the last AST element containing "cute::suite"
	//issue of multi suite in a file
	@Override
	public int leave(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = declStmt.getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) decl;
				IASTDeclSpecifier declSpec = sDecl.getDeclSpecifier();
				if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
					ICPPASTNamedTypeSpecifier nDeclSpec = (ICPPASTNamedTypeSpecifier) declSpec;
					if(nDeclSpec.getName().toString().equals("cute::suite")) {
						suiteDeclName = sDecl.getDeclarators()[0].getName();
					}
					
				}
			}
		}
		return super.leave(statement);
	}
	
	public IASTName getSuiteDeclName() {
		return suiteDeclName;
	}
}
