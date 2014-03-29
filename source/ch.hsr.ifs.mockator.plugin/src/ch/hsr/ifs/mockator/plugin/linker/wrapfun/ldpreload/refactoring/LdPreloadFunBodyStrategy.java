package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;

interface LdPreloadFunBodyStrategy {
  IASTCompoundStatement getPreloadFunBody(CppStandard cppStd, ICPPASTFunctionDeclarator function);
}
