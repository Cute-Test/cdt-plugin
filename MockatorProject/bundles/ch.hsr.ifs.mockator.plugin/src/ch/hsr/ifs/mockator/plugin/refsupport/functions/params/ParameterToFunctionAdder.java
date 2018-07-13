package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;


public class ParameterToFunctionAdder {

   private final ICPPASTFunctionDeclarator funDecl;

   public ParameterToFunctionAdder(final ICPPASTFunctionDeclarator funDecl) {
      this.funDecl = funDecl;
   }

   public void addParametersFromFunCall(final ICPPASTFunctionCallExpression funCall) {
      final FunctionCallParameterCollector ex = new FunctionCallParameterCollector(funCall);
      final List<ICPPASTParameterDeclaration> parameters = ex.getFunctionParameters();
      addParametersToDeclarator(parameters);
   }

   private void addParametersToDeclarator(final List<ICPPASTParameterDeclaration> parameters) {
      for (final ICPPASTParameterDeclaration each : parameters) {
         funDecl.addParameterDeclaration(each);
      }
   }
}
