package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


interface LdPreloadFunBodyStrategy {

   IASTCompoundStatement getPreloadFunBody(CppStandard cppStd, ICPPASTFunctionDeclarator function);
}
