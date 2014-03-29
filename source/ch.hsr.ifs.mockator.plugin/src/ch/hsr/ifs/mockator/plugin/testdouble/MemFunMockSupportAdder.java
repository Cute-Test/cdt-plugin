package ch.hsr.ifs.mockator.plugin.testdouble;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

public interface MemFunMockSupportAdder {
  void addMockSupport(ICPPASTFunctionDefinition function);
}
