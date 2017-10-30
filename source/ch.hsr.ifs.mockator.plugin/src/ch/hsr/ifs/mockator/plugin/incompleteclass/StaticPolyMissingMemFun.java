package ch.hsr.ifs.mockator.plugin.incompleteclass;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;


public interface StaticPolyMissingMemFun extends MissingMemberFunction {

   ICPPASTFunctionDefinition getContainingFunction();

   boolean isCallEquivalent(ICPPASTFunctionDefinition function, ConstStrategy strategy);
}
