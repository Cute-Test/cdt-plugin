package ch.hsr.ifs.mockator.plugin.refsupport.functions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunArgumentsTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunctionParamTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParamTypeEquivalenceTester;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


public class FunctionEquivalenceVerifier {

   public enum ConstStrategy {
      ConsiderConst, IgnoreConst
   }

   private final ICPPASTFunctionDeclarator funDecl;

   public FunctionEquivalenceVerifier(ICPPASTFunctionDeclarator funDecl) {
      this.funDecl = funDecl;
   }

   public boolean isEquivalent(ICPPASTFunctionDeclarator other) {
      if (!funDecl.getName().toString().equals(other.getName().toString())) return false;

      ParamTypeEquivalenceTester tester = new ParamTypeEquivalenceTester(getParameterTypes(funDecl), getParameterTypes(other));
      return tester.areParametersEquivalent() && funDecl.isConst() == other.isConst();
   }

   private static List<IType> getParameterTypes(ICPPASTFunctionDeclarator funDecl) {
      FunctionParamTypeCollector helper = new FunctionParamTypeCollector(funDecl);
      return helper.getParameterTypes();
   }

   public boolean isEquivalent(ICPPASTFunctionCallExpression functionCall, ConstStrategy constStrategy) {
      if (!funDecl.getName().toString().equals(AstUtil.getName(functionCall).toString())) return false;

      boolean result = areParamsEquivalentToArguments(functionCall, funDecl);

      if (constStrategy == ConstStrategy.ConsiderConst) {
         result = result && funDecl.isConst();
      }

      return result;
   }

   private static boolean areParamsEquivalentToArguments(ICPPASTFunctionCallExpression funCall, ICPPASTFunctionDeclarator function) {
      FunArgumentsTypeCollector ex = new FunArgumentsTypeCollector(list(funCall.getArguments()));
      FunctionParamTypeCollector helper = new FunctionParamTypeCollector(function);
      List<IType> paramTypes = helper.getParameterTypes();
      List<IType> funArgTypes = ex.getFunArgTypes();
      ParamTypeEquivalenceTester tester = new ParamTypeEquivalenceTester(funArgTypes, paramTypes);
      return tester.areParametersEquivalent();
   }
}
