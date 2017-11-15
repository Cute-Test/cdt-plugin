package ch.hsr.ifs.mockator.plugin.refsupport.functions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


public class FunctionDelegateCallCreator {

   private static final ICPPNodeFactory    nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTFunctionDeclarator function;
   private final Collection<Integer>       paramPositionsToIgnore;

   public FunctionDelegateCallCreator(final ICPPASTFunctionDeclarator function) {
      this(function, new HashSet<Integer>());
   }

   public FunctionDelegateCallCreator(final ICPPASTFunctionDeclarator function, final Collection<Integer> paramPositionsToIgnore) {
      this.function = function;
      this.paramPositionsToIgnore = paramPositionsToIgnore;
   }

   public IASTStatement createDelegate(final IASTName funName) {
      return createDelegate(funName, ASTUtil.getDeclSpec(function));
   }

   public IASTStatement createDelegate(final IASTName funName, final IASTDeclSpecifier declSpec) {
      final ICPPASTFunctionCallExpression call = createFunCall(funName);

      if (ASTUtil.isVoid(declSpec) && hasNoPointers()) {
         return nodeFactory.newExpressionStatement(call);
      }

      return nodeFactory.newReturnStatement(call);
   }

   private ICPPASTFunctionCallExpression createFunCall(final IASTName funName) {
      final IASTIdExpression idExpr = nodeFactory.newIdExpression(funName.copy());
      return nodeFactory.newFunctionCallExpression(idExpr, getFunctionArgs());
   }

   private boolean hasNoPointers() {
      return function.getPointerOperators().length == 0;
   }

   private IASTInitializerClause[] getFunctionArgs() {
      final ICPPASTParameterDeclaration[] params = function.getParameters();
      final IASTInitializerClause[] args = new IASTInitializerClause[params.length - paramPositionsToIgnore.size()];
      final ParameterNameCreator paramNameCreator = getParamNameCreator();

      for (int i = 0; i < params.length; ++i) {
         if (!paramPositionsToIgnore.contains(i)) {
            args[i] = createExpression(params[i], paramNameCreator);
         }
      }

      return args;
   }

   private static IASTIdExpression createExpression(final ICPPASTParameterDeclaration param, final ParameterNameCreator nameCreator) {
      String paramName = param.getDeclarator().getName().toString();

      if (paramName.isEmpty() && !ASTUtil.isVoid(param)) {
         final IType paramType = TypeCreator.byParamDeclaration(param);
         paramName = nameCreator.getParamName(paramType).toString();
      }

      return nodeFactory.newIdExpression(nodeFactory.newName(paramName.toCharArray()));
   }

   private static ParameterNameCreator getParamNameCreator() {
      final Map<String, Boolean> nameHistory = unorderedMap();
      final ParameterNameCreator nameCreator = new ParameterNameCreator(nameHistory);
      return nameCreator;
   }
}
