package ch.hsr.ifs.mockator.plugin.testdouble;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;

public interface CallRegistrationFinder {
  Maybe<? extends MemFunSignature> findRegisteredCall(ICPPASTFunctionDefinition function);
}
