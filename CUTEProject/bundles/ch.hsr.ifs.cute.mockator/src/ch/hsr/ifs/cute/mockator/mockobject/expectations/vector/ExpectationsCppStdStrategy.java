package ch.hsr.ifs.cute.mockator.mockobject.expectations.vector;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


public interface ExpectationsCppStdStrategy {

   Collection<IASTStatement> createExpectationsVector(Collection<? extends TestDoubleMemFun> memFuns, String vectorName,
         ICPPASTFunctionDefinition testFun, Optional<IASTName> expectationsVector, LinkedEditModeStrategy strategy);
}
