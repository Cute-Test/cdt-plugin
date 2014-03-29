package ch.hsr.ifs.mockator.plugin.refsupport.functions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;

@SuppressWarnings("restriction")
public class FunctionDelegateCallCreator {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ICPPASTFunctionDeclarator function;
  private final Collection<Integer> paramPositionsToIgnore;

  public FunctionDelegateCallCreator(ICPPASTFunctionDeclarator function) {
    this(function, new HashSet<Integer>());
  }

  public FunctionDelegateCallCreator(ICPPASTFunctionDeclarator function,
      Collection<Integer> paramPositionsToIgnore) {
    this.function = function;
    this.paramPositionsToIgnore = paramPositionsToIgnore;
  }

  public IASTStatement createDelegate(IASTName funName) {
    return createDelegate(funName, AstUtil.getDeclSpec(function));
  }

  public IASTStatement createDelegate(IASTName funName, IASTDeclSpecifier declSpec) {
    ICPPASTFunctionCallExpression call = createFunCall(funName);

    if (AstUtil.isVoid(declSpec) && hasNoPointers())
      return nodeFactory.newExpressionStatement(call);

    return nodeFactory.newReturnStatement(call);
  }

  private ICPPASTFunctionCallExpression createFunCall(IASTName funName) {
    IASTIdExpression idExpr = nodeFactory.newIdExpression(funName.copy());
    return nodeFactory.newFunctionCallExpression(idExpr, getFunctionArgs());
  }

  private boolean hasNoPointers() {
    return function.getPointerOperators().length == 0;
  }

  private IASTInitializerClause[] getFunctionArgs() {
    ICPPASTParameterDeclaration[] params = function.getParameters();
    IASTInitializerClause[] args =
        new IASTInitializerClause[params.length - paramPositionsToIgnore.size()];
    ParameterNameCreator paramNameCreator = getParamNameCreator();

    for (int i = 0; i < params.length; ++i) {
      if (!paramPositionsToIgnore.contains(i)) {
        args[i] = createExpression(params[i], paramNameCreator);
      }
    }

    return args;
  }

  private static IASTIdExpression createExpression(ICPPASTParameterDeclaration param,
      ParameterNameCreator nameCreator) {
    String paramName = param.getDeclarator().getName().toString();

    if (paramName.isEmpty() && !AstUtil.isVoid(param)) {
      IType paramType = TypeCreator.byParamDeclaration(param);
      paramName = nameCreator.getParamName(paramType).toString();
    }

    return nodeFactory.newIdExpression(nodeFactory.newName(paramName.toCharArray()));
  }

  private static ParameterNameCreator getParamNameCreator() {
    Map<String, Boolean> nameHistory = unorderedMap();
    ParameterNameCreator nameCreator = new ParameterNameCreator(nameHistory);
    return nameCreator;
  }
}
