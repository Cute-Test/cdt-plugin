package ch.hsr.ifs.cute.mockator.incompleteclass;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


public interface MissingMemberFunction extends TestDoubleMemFun {

    ICPPASTFunctionDefinition createFunctionDefinition(TestDoubleMemFunImplStrategy strategy, CppStandard cppStd);
}
