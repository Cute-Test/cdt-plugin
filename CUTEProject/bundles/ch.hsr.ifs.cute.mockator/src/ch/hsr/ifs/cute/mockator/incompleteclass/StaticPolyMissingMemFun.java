package ch.hsr.ifs.cute.mockator.incompleteclass;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;


public interface StaticPolyMissingMemFun extends MissingMemberFunction {

    ICPPASTFunctionDefinition getContainingFunction();

    boolean isCallEquivalent(ICPPASTFunctionDefinition function, ConstStrategy strategy);
}
