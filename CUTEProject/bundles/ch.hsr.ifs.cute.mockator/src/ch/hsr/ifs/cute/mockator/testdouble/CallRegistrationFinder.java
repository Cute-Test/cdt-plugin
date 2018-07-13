package ch.hsr.ifs.cute.mockator.testdouble;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;


public interface CallRegistrationFinder {

   Optional<? extends MemFunSignature> findRegisteredCall(ICPPASTFunctionDefinition function);
}
