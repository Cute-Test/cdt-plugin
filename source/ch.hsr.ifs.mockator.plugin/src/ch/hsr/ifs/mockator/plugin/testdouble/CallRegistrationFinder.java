package ch.hsr.ifs.mockator.plugin.testdouble;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;


public interface CallRegistrationFinder {

   Optional<? extends MemFunSignature> findRegisteredCall(ICPPASTFunctionDefinition function);
}
