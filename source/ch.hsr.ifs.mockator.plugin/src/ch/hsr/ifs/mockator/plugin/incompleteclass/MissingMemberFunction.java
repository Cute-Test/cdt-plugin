package ch.hsr.ifs.mockator.plugin.incompleteclass;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


public interface MissingMemberFunction extends TestDoubleMemFun {

   ICPPASTFunctionDefinition createFunctionDefinition(TestDoubleMemFunImplStrategy strategy, CppStandard cppStd);
}
