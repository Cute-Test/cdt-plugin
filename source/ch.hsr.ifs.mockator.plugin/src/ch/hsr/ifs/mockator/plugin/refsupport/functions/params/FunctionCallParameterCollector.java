package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class FunctionCallParameterCollector {
  private final ICPPASTFunctionCallExpression funCall;

  public FunctionCallParameterCollector(ICPPASTFunctionCallExpression funCall) {
    this.funCall = funCall;
  }

  public List<ICPPASTParameterDeclaration> getFunctionParameters() {
    Map<String, Boolean> nameHistory = unorderedMap();
    List<ICPPASTParameterDeclaration> params = list();

    for (IASTInitializerClause arg : funCall.getArguments()) {
      IASTExpression idExpr = AstUtil.getChildOfType(arg, IASTExpression.class);

      if (idExpr == null) {
        continue;
      }

      params.add(ParamDeclCreator.createParameterFrom(idExpr, nameHistory));
    }

    return params;
  }
}
