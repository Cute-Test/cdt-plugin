package ch.hsr.ifs.cute.mockator.incompleteclass;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;


public interface TestDoubleMemFunImplStrategy {

   void addCallVectorRegistration(IASTCompoundStatement body, ICPPASTFunctionDeclarator fun, boolean isStatic);

   void addCtorInitializer(ICPPASTFunctionDefinition ctor);
}
