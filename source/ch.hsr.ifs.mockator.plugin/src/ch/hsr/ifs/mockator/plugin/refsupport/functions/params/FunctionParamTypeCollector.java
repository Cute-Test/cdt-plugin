package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


public class FunctionParamTypeCollector {

   private final ICPPASTFunctionDeclarator function;

   public FunctionParamTypeCollector(final ICPPASTFunctionDeclarator function) {
      this.function = function;
   }

   public List<IType> getParameterTypes() {
      final List<IType> paramTypes = list();

      for (final ICPPASTParameterDeclaration param : function.getParameters()) {
         paramTypes.add(getTypeOfParam(param));
      }

      return paramTypes;
   }

   private static IType getTypeOfParam(final ICPPASTParameterDeclaration param) {
      return TypeCreator.byDeclarator(param.getDeclarator());
   }
}
