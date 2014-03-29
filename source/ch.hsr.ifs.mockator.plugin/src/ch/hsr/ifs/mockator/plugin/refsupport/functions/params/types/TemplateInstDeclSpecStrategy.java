package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

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

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.collections.ParallelIterator;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypedefHelper;

@SuppressWarnings("restriction")
class TemplateInstDeclSpecStrategy implements DeclSpecGeneratorStrategy {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  @Override
  public ICPPASTDeclSpecifier createDeclSpec(IType type) {
    Assert.instanceOf(type, ICPPTemplateInstance.class,
        "This strategy can only handle template instances");
    ICPPTemplateInstance templateInstance = (ICPPTemplateInstance) type;
    ICPPASTTemplateId templateId = nodeFactory.newTemplateId(getName(templateInstance));
    ParallelIterator<ICPPTemplateParameter, ICPPTemplateArgument> it =
        getParallelIt(templateInstance);

    while (it.hasNext()) {
      Pair<ICPPTemplateParameter, ICPPTemplateArgument> next = it.next();
      ICPPTemplateParameter templateParam = _1(next);
      ICPPTemplateArgument templateArg = _2(next);

      if (considerTemplateArgument(templateParam.getDefaultValue(), templateArg)) {
        templateId.addTemplateArgument(toASTTypeId(templateArg));
      }
    }

    return nodeFactory.newTypedefNameSpecifier(templateId);
  }

  private static boolean considerTemplateArgument(ICPPTemplateArgument defaultV,
      ICPPTemplateArgument templateArg) {
    return defaultV == null
        || !isDefValEqualsToTemplateArg(defaultV.getTypeValue(), templateArg.getTypeValue());
  }

  private static IASTName getName(ICPPTemplateInstance templateInst) {
    ICPPTemplateDefinition templateDefinition = templateInst.getTemplateDefinition();
    return nodeFactory.newName(getQName(templateDefinition));
  }

  private static char[] getQName(ICPPTemplateDefinition templateDefinition) {
    return AstUtil.getQfName(templateDefinition).toCharArray();
  }

  private static boolean isDefValEqualsToTemplateArg(IType defaultValType, IType argTypeVal) {
    // FIXME bit hacky...
    String defaultValue = ASTTypeUtil.getType(defaultValType);
    String argValue = ASTTypeUtil.getType(argTypeVal);
    String regex = defaultValue.replaceAll("#\\d", ".*?");
    return argValue.matches(regex);
  }

  private static ParallelIterator<ICPPTemplateParameter, ICPPTemplateArgument> getParallelIt(
      ICPPTemplateInstance ti) {
    ICPPTemplateDefinition templateDefinition = ti.getTemplateDefinition();
    Iterator<ICPPTemplateParameter> paramsIt = getTemplateParamIterator(templateDefinition);
    Iterator<ICPPTemplateArgument> argsIt = getTemplateArgIterator(ti);
    return new ParallelIterator<ICPPTemplateParameter, ICPPTemplateArgument>(paramsIt, argsIt);
  }

  private static Iterator<ICPPTemplateArgument> getTemplateArgIterator(
      ICPPTemplateInstance templateInstance) {
    return list(templateInstance.getTemplateArguments()).iterator();
  }

  private static Iterator<ICPPTemplateParameter> getTemplateParamIterator(
      ICPPTemplateDefinition templateDef) {
    return list(templateDef.getTemplateParameters()).iterator();
  }

  private static IASTTypeId toASTTypeId(ICPPTemplateArgument templateArgument) {
    IType type = templateArgument.getTypeValue();
    String shortestType = getShortestType(type);
    ICPPASTSimpleDeclSpecifier newDeclSpec = nodeFactory.newSimpleDeclSpecifier();
    ICPPASTDeclarator newDeclarator =
        nodeFactory.newDeclarator(nodeFactory.newName(shortestType.toCharArray()));
    return nodeFactory.newTypeId(newDeclSpec, newDeclarator);
  }

  private static String getShortestType(IType type) {
    try {
      return new TypedefHelper(type).findShortestType();
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }
}
