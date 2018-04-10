package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import ch.hsr.ifs.iltis.cpp.wrappers.CPPVisitor;


public class FunctionCallParameterCollector {

   private final ICPPASTFunctionCallExpression funCall;

   public FunctionCallParameterCollector(final ICPPASTFunctionCallExpression funCall) {
      this.funCall = funCall;
   }

   public List<ICPPASTParameterDeclaration> getFunctionParameters() {
      final Map<String, Boolean> nameHistory = new HashMap<>();
      final List<ICPPASTParameterDeclaration> params = new ArrayList<>();

      for (final IASTInitializerClause arg : funCall.getArguments()) {
         final IASTExpression idExpr = CPPVisitor.findChildWithType(arg, IASTExpression.class).orElse(null);

         if (idExpr == null) {
            continue;
         }

         params.add(ParamDeclCreator.createParameterFrom(idExpr, nameHistory));
      }

      return params;
   }
}
