package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class ClassDefinitionLookup implements F1V<ExtractInterfaceContext> {

  @Override
  public void apply(ExtractInterfaceContext context) {
    IASTName classNameToLookup = getNameOfSelectedSpecifier(context.getSelectedName());
    ICPPASTCompositeTypeSpecifier classDefinition = null;

    if (classNameToLookup != null) {
      classDefinition = lookupInAst(classNameToLookup);

      if (classDefinition == null) {
        classDefinition = lookupInIndex(context, classNameToLookup);
      }
    }

    if (classDefinition == null) {
      warnUserAboutMissingClass(context.getStatus());
    } else {
      rememberClassInformation(context, classDefinition, getSutClass(classNameToLookup));
    }
  }

  private static ICPPASTCompositeTypeSpecifier lookupInAst(IASTName classNameToLookup) {
    IBinding binding = classNameToLookup.resolveBinding();

    for (IASTName name : classNameToLookup.getTranslationUnit().getDefinitionsInAST(binding)) {
      for (ICPPASTCompositeTypeSpecifier optClass : getClass(name))
        return optClass;
    }

    return null;
  }

  private static ICPPASTCompositeTypeSpecifier lookupInIndex(ExtractInterfaceContext context,
      IASTName classNameToLookup) {
    for (ICPPASTCompositeTypeSpecifier optClass : findClassDefinition(classNameToLookup, context))
      return optClass;
    return null;
  }

  private static Maybe<ICPPASTCompositeTypeSpecifier> findClassDefinition(
      IASTName classNameToLookup, ExtractInterfaceContext context) {
    NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
    return lookup.findClassDefinition(classNameToLookup, context.getCRefContext());
  }

  private static IASTName getNameOfSelectedSpecifier(IASTName selectedName) {
    for (ICPPASTNamedTypeSpecifier optSpec : getNamedSpecifier(selectedName))
      return optSpec.getName();
    return selectedName;
  }

  private static Maybe<ICPPASTNamedTypeSpecifier> getNamedSpecifier(IASTNode originalNode) {
    ICPPASTNamedTypeSpecifier namedSpec =
        AstUtil.getAncestorOfType(originalNode, ICPPASTNamedTypeSpecifier.class);

    if (namedSpec != null)
      return maybe(namedSpec);

    for (IASTDeclSpecifier optDeclSpec : AstUtil.getDeclarationSpecifier(originalNode))
      if (optDeclSpec instanceof ICPPASTNamedTypeSpecifier)
        return maybe((ICPPASTNamedTypeSpecifier) optDeclSpec);

    return none();
  }

  private static void warnUserAboutMissingClass(RefactoringStatus status) {
    status.addFatalError("No class found to extract an interface from!");
  }

  private static Maybe<ICPPASTCompositeTypeSpecifier> getSutClass(IASTName selectedName) {
    for (ICPPASTCompositeTypeSpecifier optClass : getClass(selectedName))
      if (!optClass.getName().toString().equals(selectedName.toString()))
        return maybe(optClass);

    return none();
  }

  private static Maybe<ICPPASTCompositeTypeSpecifier> getClass(IASTName originalNode) {
    ICPPASTCompositeTypeSpecifier parentClass =
        AstUtil.getAncestorOfType(originalNode, ICPPASTCompositeTypeSpecifier.class);
    return maybe(parentClass);
  }

  private static void rememberClassInformation(ExtractInterfaceContext context,
      ICPPASTCompositeTypeSpecifier dependency, Maybe<ICPPASTCompositeTypeSpecifier> sutClass) {
    context.setChosenClass(dependency);
    context.setTuOfChosenClass(dependency.getTranslationUnit());

    for (ICPPASTCompositeTypeSpecifier optSutClass : sutClass) {
      context.setSutClass(optSutClass);
    }
  }
}
