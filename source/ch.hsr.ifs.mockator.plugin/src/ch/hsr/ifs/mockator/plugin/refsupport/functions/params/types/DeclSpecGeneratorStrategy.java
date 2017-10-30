package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;


interface DeclSpecGeneratorStrategy {

   ICPPASTDeclSpecifier createDeclSpec(IType type);
}
