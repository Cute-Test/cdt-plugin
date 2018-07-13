package ch.hsr.ifs.cute.mockator.fakeobject;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;


class FakeObjectMemFunImplStrategy implements TestDoubleMemFunImplStrategy {

   @Override
   public void addCtorInitializer(final ICPPASTFunctionDefinition ctor) {}

   @Override
   public void addCallVectorRegistration(final IASTCompoundStatement body, final ICPPASTFunctionDeclarator decl, final boolean isStatic) {}
}
