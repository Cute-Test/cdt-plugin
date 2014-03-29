package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

public class ParameterToFunctionAdder {
  private final ICPPASTFunctionDeclarator funDecl;

  public ParameterToFunctionAdder(ICPPASTFunctionDeclarator funDecl) {
    this.funDecl = funDecl;
  }

  public void addParametersFromFunCall(ICPPASTFunctionCallExpression funCall) {
    FunctionCallParameterCollector ex = new FunctionCallParameterCollector(funCall);
    List<ICPPASTParameterDeclaration> parameters = ex.getFunctionParameters();
    addParametersToDeclarator(parameters);
  }

  private void addParametersToDeclarator(List<ICPPASTParameterDeclaration> parameters) {
    for (ICPPASTParameterDeclaration each : parameters) {
      funDecl.addParameterDeclaration(each);
    }
  }
}
