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

   public FunctionEquivalenceVerifier(final ICPPASTFunctionDeclarator funDecl) {
      this.funDecl = funDecl;
   }

   public boolean isEquivalent(final ICPPASTFunctionDeclarator other) {
      if (!funDecl.getName().toString().equals(other.getName().toString())) return false;

      final ParamTypeEquivalenceTester tester = new ParamTypeEquivalenceTester(getParameterTypes(funDecl), getParameterTypes(other));
      return tester.areParametersEquivalent() && funDecl.isConst() == other.isConst();
   }

   private static List<IType> getParameterTypes(final ICPPASTFunctionDeclarator funDecl) {
      final FunctionParamTypeCollector helper = new FunctionParamTypeCollector(funDecl);
      return helper.getParameterTypes();
   }

   public boolean isEquivalent(final ICPPASTFunctionCallExpression functionCall, final ConstStrategy constStrategy) {
      if (!funDecl.getName().toString().equals(AstUtil.getName(functionCall).toString())) return false;

      boolean result = areParamsEquivalentToArguments(functionCall, funDecl);

      if (constStrategy == ConstStrategy.ConsiderConst) {
         result = result && funDecl.isConst();
      }

      return result;
   }

   private static boolean areParamsEquivalentToArguments(final ICPPASTFunctionCallExpression funCall, final ICPPASTFunctionDeclarator function) {
      final FunArgumentsTypeCollector ex = new FunArgumentsTypeCollector(list(funCall.getArguments()));
      final FunctionParamTypeCollector helper = new FunctionParamTypeCollector(function);
      final List<IType> paramTypes = helper.getParameterTypes();
      final List<IType> funArgTypes = ex.getFunArgTypes();
      final ParamTypeEquivalenceTester tester = new ParamTypeEquivalenceTester(funArgTypes, paramTypes);
      return tester.areParametersEquivalent();
   }
}
