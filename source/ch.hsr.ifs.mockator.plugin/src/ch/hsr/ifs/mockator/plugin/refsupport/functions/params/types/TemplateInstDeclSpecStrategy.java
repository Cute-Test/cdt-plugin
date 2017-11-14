package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import java.util.stream.Stream;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.Functional;
import ch.hsr.ifs.iltis.core.functional.StreamPair;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypedefHelper;


class TemplateInstDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
      Assert.instanceOf(type, ICPPTemplateInstance.class, "This strategy can only handle template instances");
      final ICPPTemplateInstance templateInstance = (ICPPTemplateInstance) type;
      final ICPPASTTemplateId templateId = nodeFactory.newTemplateId(getName(templateInstance));

      getZipedStream(templateInstance).forEachOrdered((pair) -> {
         final ICPPTemplateParameter templateParam = pair.first();
         final ICPPTemplateArgument templateArg = pair.second();

         if (considerTemplateArgument(templateParam.getDefaultValue(), templateArg)) {
            templateId.addTemplateArgument(toASTTypeId(templateArg));
         }
      });

      return nodeFactory.newTypedefNameSpecifier(templateId);
   }

   private static boolean considerTemplateArgument(final ICPPTemplateArgument defaultV, final ICPPTemplateArgument templateArg) {
      return defaultV == null || !isDefValEqualsToTemplateArg(defaultV.getTypeValue(), templateArg.getTypeValue());
   }

   private static IASTName getName(final ICPPTemplateInstance templateInst) {
      final ICPPTemplateDefinition templateDefinition = templateInst.getTemplateDefinition();
      return nodeFactory.newName(getQName(templateDefinition));
   }

   private static char[] getQName(final ICPPTemplateDefinition templateDefinition) {
      return AstUtil.getQfName(templateDefinition).toCharArray();
   }

   private static boolean isDefValEqualsToTemplateArg(final IType defaultValType, final IType argTypeVal) {
      // FIXME bit hacky...
      final String defaultValue = ASTTypeUtil.getType(defaultValType);
      final String argValue = ASTTypeUtil.getType(argTypeVal);
      final String regex = defaultValue.replaceAll("#\\d", ".*?");
      return argValue.matches(regex);
   }

   private static Stream<StreamPair<ICPPTemplateParameter, ICPPTemplateArgument>> getZipedStream(final ICPPTemplateInstance ti) {
      final ICPPTemplateDefinition templateDefinition = ti.getTemplateDefinition();
      return Functional.zip(templateDefinition.getTemplateParameters(), ti.getTemplateArguments());
   }

   private static IASTTypeId toASTTypeId(final ICPPTemplateArgument templateArgument) {
      final IType type = templateArgument.getTypeValue();
      final String shortestType = getShortestType(type);
      final ICPPASTSimpleDeclSpecifier newDeclSpec = nodeFactory.newSimpleDeclSpecifier();
      final ICPPASTDeclarator newDeclarator = nodeFactory.newDeclarator(nodeFactory.newName(shortestType.toCharArray()));
      return nodeFactory.newTypeId(newDeclSpec, newDeclarator);
   }

   private static String getShortestType(final IType type) {
      try {
         return new TypedefHelper(type).findShortestType();
      }
      catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
