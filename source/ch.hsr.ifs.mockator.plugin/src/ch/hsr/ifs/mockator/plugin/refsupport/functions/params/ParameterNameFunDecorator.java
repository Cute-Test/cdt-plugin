package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


@SuppressWarnings("restriction")
public class ParameterNameFunDecorator {

   private static final CPPNodeFactory     nodeFactory = CPPNodeFactory.getDefault();
   private final ICPPASTFunctionDeclarator function;

   public ParameterNameFunDecorator(ICPPASTFunctionDeclarator function) {
      this.function = function;
   }

   public void adjustParamNamesIfNecessary() {
      ParameterNameCreator nameCreator = getParamNameCreator();

      for (ICPPASTParameterDeclaration param : function.getParameters()) {
         ICPPASTDeclarator declarator = param.getDeclarator();
         String paramName = declarator.getName().toString();

         if (!(paramName.isEmpty() && !isVoid(param))) {
            continue;
         }

         IType type = TypeCreator.byParamDeclaration(param);

         if (type instanceof IProblemType && isNamedSpecifier(param)) {
            String typeName = ((ICPPASTNamedTypeSpecifier) param.getDeclSpecifier()).getName().toString();
            paramName = nameCreator.getParamName(typeName).toString();
         } else {
            paramName = nameCreator.getParamName(type).toString();
         }

         declarator.setName(nodeFactory.newName(paramName.toCharArray()));
      }
   }

   private static boolean isNamedSpecifier(ICPPASTParameterDeclaration param) {
      return param.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier;
   }

   private static ParameterNameCreator getParamNameCreator() {
      Map<String, Boolean> nameHistory = unorderedMap();
      return new ParameterNameCreator(nameHistory);
   }

   private static boolean isVoid(ICPPASTParameterDeclaration param) {
      return AstUtil.isVoid(param) && param.getDeclarator().getPointerOperators().length == 0;
   }
}
