package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


public class ParameterNameFunDecorator {

   private static final ICPPNodeFactory    nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTFunctionDeclarator function;

   public ParameterNameFunDecorator(final ICPPASTFunctionDeclarator function) {
      this.function = function;
   }

   public void adjustParamNamesIfNecessary() {
      final ParameterNameCreator nameCreator = getParamNameCreator();

      for (final ICPPASTParameterDeclaration param : function.getParameters()) {
         final ICPPASTDeclarator declarator = param.getDeclarator();
         String paramName = declarator.getName().toString();

         if (!(paramName.isEmpty() && !isVoid(param))) {
            continue;
         }

         final IType type = TypeCreator.byParamDeclaration(param);

         if (type instanceof IProblemType && isNamedSpecifier(param)) {
            final String typeName = ((ICPPASTNamedTypeSpecifier) param.getDeclSpecifier()).getName().toString();
            paramName = nameCreator.getParamName(typeName).toString();
         } else {
            paramName = nameCreator.getParamName(type).toString();
         }

         declarator.setName(nodeFactory.newName(paramName.toCharArray()));
      }
   }

   private static boolean isNamedSpecifier(final ICPPASTParameterDeclaration param) {
      return param.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier;
   }

   private static ParameterNameCreator getParamNameCreator() {
      final Map<String, Boolean> nameHistory = unorderedMap();
      return new ParameterNameCreator(nameHistory);
   }

   private static boolean isVoid(final ICPPASTParameterDeclaration param) {
      return AstUtil.isVoid(param) && param.getDeclarator().getPointerOperators().length == 0;
   }
}
