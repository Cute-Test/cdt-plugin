package ch.hsr.ifs.cute.mockator.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;


interface DeclSpecGeneratorStrategy {

   static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   ICPPASTDeclSpecifier createDeclSpec(IType type);
}
