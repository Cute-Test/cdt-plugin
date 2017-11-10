package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Iterator;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.mockator.plugin.base.collections.ParallelIterator;
import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypedefHelper;


@SuppressWarnings("restriction")
class TemplateInstDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
      Assert.instanceOf(type, ICPPTemplateInstance.class, "This strategy can only handle template instances");
      final ICPPTemplateInstance templateInstance = (ICPPTemplateInstance) type;
      final ICPPASTTemplateId templateId = nodeFactory.newTemplateId(getName(templateInstance));
      final ParallelIterator<ICPPTemplateParameter, ICPPTemplateArgument> it = getParallelIt(templateInstance);

      while (it.hasNext()) {
         final Pair<ICPPTemplateParameter, ICPPTemplateArgument> next = it.next();
         final ICPPTemplateParameter templateParam = next.first();
         final ICPPTemplateArgument templateArg = next.second();

         if (considerTemplateArgument(templateParam.getDefaultValue(), templateArg)) {
            templateId.addTemplateArgument(toASTTypeId(templateArg));
         }
      }

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

   private static ParallelIterator<ICPPTemplateParameter, ICPPTemplateArgument> getParallelIt(final ICPPTemplateInstance ti) {
      final ICPPTemplateDefinition templateDefinition = ti.getTemplateDefinition();
      final Iterator<ICPPTemplateParameter> paramsIt = getTemplateParamIterator(templateDefinition);
      final Iterator<ICPPTemplateArgument> argsIt = getTemplateArgIterator(ti);
      return new ParallelIterator<>(paramsIt, argsIt);
   }

   private static Iterator<ICPPTemplateArgument> getTemplateArgIterator(final ICPPTemplateInstance templateInstance) {
      return list(templateInstance.getTemplateArguments()).iterator();
   }

   private static Iterator<ICPPTemplateParameter> getTemplateParamIterator(final ICPPTemplateDefinition templateDef) {
      return list(templateDef.getTemplateParameters()).iterator();
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
