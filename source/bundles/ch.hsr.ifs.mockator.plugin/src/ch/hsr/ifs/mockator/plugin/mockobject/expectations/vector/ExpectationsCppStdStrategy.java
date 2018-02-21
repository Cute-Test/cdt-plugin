package ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


public interface ExpectationsCppStdStrategy {

   Collection<IASTStatement> createExpectationsVector(Collection<? extends TestDoubleMemFun> memFuns, String vectorName,
         ICPPASTFunctionDefinition testFun, Optional<IASTName> expectationsVector, LinkedEditModeStrategy strategy);
}
