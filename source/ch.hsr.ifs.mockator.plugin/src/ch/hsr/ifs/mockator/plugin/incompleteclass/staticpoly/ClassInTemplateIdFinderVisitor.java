package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
class ClassInTemplateIdFinderVisitor extends ASTVisitor {
  private final ICPPASTCompositeTypeSpecifier testDouble;
  private final Set<Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter>> templateParams;
  private final ICProject cProject;
  private final IIndex index;

  {
    shouldVisitNames = true;
  }

  public ClassInTemplateIdFinderVisitor(ICPPASTCompositeTypeSpecifier testDouble,
      ICProject cProject, IIndex index) {
    this.testDouble = testDouble;
    this.cProject = cProject;
    this.index = index;
    templateParams = orderPreservingSet();
  }

  public Collection<Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter>> getTemplateParamCombinations() {
    return templateParams;
  }

  @Override
  public int visit(IASTName name) {
    IBinding binding = name.resolveBinding();

    if (!(binding instanceof ICPPTemplateInstance))
      return PROCESS_CONTINUE;

    processTemplateArguments((ICPPTemplateInstance) binding, name);
    return PROCESS_SKIP;
  }

  private void processTemplateArguments(ICPPTemplateInstance templateInstance, IASTName name) {
    IType testDoubleType = CPPVisitor.createType(testDouble);
    ICPPTemplateArgument[] templateArguments = templateInstance.getTemplateArguments();
    List<Integer> positions = list();

    for (int i = 0; i < templateArguments.length; i++) {
      IType templateArg = templateArguments[i].getTypeValue();

      if (AstUtil.isSameType(templateArg, testDoubleType) || refersToTestDouble(name, i)) {
        positions.add(i);
      }
    }

    if (positions.isEmpty())
      return;

    processTemplateDefinition(positions, templateInstance.getTemplateDefinition());
  }

  private boolean refersToTestDouble(IASTName name, int templateArgPos) {
    if (!(name instanceof ICPPASTTemplateId))
      return false;

    IASTNode node = ((ICPPASTTemplateId) name).getTemplateArguments()[templateArgPos];

    if (node instanceof ICPPASTTypeId) {
      IASTDeclSpecifier declSpecifier = ((ICPPASTTypeId) node).getDeclSpecifier();

      if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
        IASTName argName = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
        return argName.resolveBinding().equals(testDouble.getName().resolveBinding());
      }
    }

    return false;
  }

  private void processTemplateDefinition(Collection<Integer> positions,
      ICPPTemplateDefinition definition) {
    for (ICPPASTTemplateDeclaration candidate : findTemplate(definition)) {
      ICPPASTTemplateParameter[] templateParams = candidate.getTemplateParameters();

      for (Integer pos : positions) {
        Assert
            .isTrue(pos < templateParams.length, "Wrong deduction of template parameter position");
        addToTemplateParamCombinations(candidate, templateParams[pos]);
      }
    }
  }

  private void addToTemplateParamCombinations(ICPPASTTemplateDeclaration templateDecl,
      ICPPASTTemplateParameter templateParam) {
    templateParams.add(Tuple.from(templateDecl, templateParam));
  }

  private static Collection<ICPPASTTemplateDeclaration> lookupInAst(IASTName name) {
    ICPPASTTemplateDeclaration templateDecl =
        AstUtil.getAncestorOfType(name, ICPPASTTemplateDeclaration.class);
    return list(templateDecl);
  }

  private Collection<ICPPASTTemplateDeclaration> findTemplate(IBinding template) {
    IASTName[] definitionNames = testDouble.getTranslationUnit().getDefinitionsInAST(template);

    if (definitionNames.length > 0)
      return lookupInAst(definitionNames[0]);

    List<ICPPASTTemplateDeclaration> templates = list();

    for (ICPPASTTemplateDeclaration candidate : lookupInIndex(template)) {
      templates.add(candidate);
    }
    return templates;
  }

  private Maybe<ICPPASTTemplateDeclaration> lookupInIndex(IBinding template) {
    NodeLookup lookup = new NodeLookup(cProject, new NullProgressMonitor());
    return lookup.findTemplateDefinition(template, index);
  }
}
