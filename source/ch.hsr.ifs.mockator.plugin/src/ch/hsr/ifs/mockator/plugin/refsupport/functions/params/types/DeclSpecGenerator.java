package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


public class DeclSpecGenerator {

   private final IType type;

   public DeclSpecGenerator(final IType type) {
      this.type = AstUtil.windDownToRealType(type, true);
   }

   public ICPPASTDeclSpecifier getDeclSpec() {
      final DeclSpecGeneratorStrategy strategy = getStrategy(type);
      return strategy.createDeclSpec(type);
   }

   private static DeclSpecGeneratorStrategy getStrategy(final IType type) {
      if (type instanceof ICPPTemplateInstance && hasDefaultTemplateParams((ICPPTemplateInstance) type)) return new TemplateInstDeclSpecStrategy();
      if (type instanceof ICPPBinding) return new BindingDeclSpecStrategy();
      else if (type instanceof IBasicType) return new BasicTypeDeclSpecStrategy();
      else return new DefaultDeclSpecStrategy();
   }

   private static boolean hasDefaultTemplateParams(final ICPPTemplateInstance templateInst) {
      final ICPPTemplateDefinition templateDefinition = templateInst.getTemplateDefinition();

      for (final ICPPTemplateParameter param : templateDefinition.getTemplateParameters()) {
         if (param.getDefaultValue() != null) return true;
      }

      return false;
   }
}
