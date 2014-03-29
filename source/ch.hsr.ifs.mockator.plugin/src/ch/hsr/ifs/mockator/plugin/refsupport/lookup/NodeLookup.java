package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class NodeLookup {
  private final ICProject originProject;
  private final IProgressMonitor pm;

  public NodeLookup(ICProject originProject, IProgressMonitor pm) {
    this.originProject = originProject;
    this.pm = pm;
  }

  public Maybe<ICPPASTFunctionDefinition> findFunctionDefinition(IASTName funName,
      CRefactoringContext context) {
    DefinitionFinder finder =
        new DefinitionFinder(originProject, getIndex(context), getTuLoader(context));

    for (IASTNode optDefinition : finder.findDefinition(funName))
      return getNodeAncestor(optDefinition, ICPPASTFunctionDefinition.class);

    return none();
  }

  public Maybe<ICPPASTFunctionDeclarator> findFunctionDeclaration(IBinding binding, IIndex index) {
    DeclarationFinder finder = new DeclarationFinder(originProject, index, getTuLoader(index));
    return getFirstFunDeclaration(binding, finder);
  }

  public Maybe<ICPPASTFunctionDeclarator> findFunctionDeclaration(IASTName funName,
      CRefactoringContext context) {
    DeclarationFinder finder =
        new DeclarationFinder(originProject, getIndex(context), getTuLoader(context));
    return getFirstFunDeclaration(funName.resolveBinding(), finder);
  }

  private static Maybe<ICPPASTFunctionDeclarator> getFirstFunDeclaration(IBinding funName,
      DeclarationFinder finder) {
    // just consider the first declaration found
    for (IASTNode optDeclaration : head(finder.findDeclarations(funName)))
      return getNodeAncestor(optDeclaration, ICPPASTFunctionDeclarator.class);

    return none();
  }

  public Maybe<ICPPASTCompositeTypeSpecifier> findClassDefinition(IASTName className,
      CRefactoringContext context) {
    DefinitionFinder finder =
        new DefinitionFinder(originProject, getIndex(context), getTuLoader(context));

    for (IASTNode optDefinition : finder.findDefinition(className))
      return getClassAncestor(optDefinition);

    return none();
  }

  private static Maybe<ICPPASTCompositeTypeSpecifier> getClassAncestor(IASTNode node) {
    return getNodeAncestor(node, ICPPASTCompositeTypeSpecifier.class);
  }

  public <T> Maybe<ICPPASTCompositeTypeSpecifier> findClassDefinition(IBinding binding, IIndex index) {
    DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));

    for (IASTNode optDefinition : finder.findDefinition(binding))
      return getClassAncestor(optDefinition);

    return none();
  }

  public Maybe<ICPPASTCompositeTypeSpecifier> findClassDefinition(String name, IIndex index) {
    DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));

    for (IASTNode optDefinition : finder.findDefinition(name))
      return getClassAncestor(optDefinition);

    return none();
  }

  public Collection<ICPPASTFunctionDefinition> findReferencingFunctions(IASTName name,
      CRefactoringContext context) {
    TranslationUnitLoader tuLoader = getTuLoader(context);
    ReferenceFinder finder = new ReferenceFinder(originProject, getIndex(context), tuLoader);
    List<ICPPASTFunctionDefinition> referencingFunctions = list();

    for (IASTNode optReference : finder.findReferences(name)) {
      for (ICPPASTFunctionDefinition optFunction : getNodeAncestor(optReference,
          ICPPASTFunctionDefinition.class)) {
        referencingFunctions.add(optFunction);
      }
    }

    return referencingFunctions;
  }

  public Maybe<ICPPASTTemplateDeclaration> findTemplateDefinition(IBinding binding, IIndex index) {
    DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));

    for (IASTNode optDefinition : finder.findDefinition(binding))
      return getNodeAncestor(optDefinition, ICPPASTTemplateDeclaration.class);

    return none();
  }

  public Collection<IASTName> findReferencingNames(IASTName name, CRefactoringContext context) {
    TranslationUnitLoader tuLoader = getTuLoader(context);
    ReferenceFinder finder = new ReferenceFinder(originProject, getIndex(context), tuLoader);
    List<IASTName> referencingNodes = list();

    for (IASTName reference : finder.findReferences(name)) {
      referencingNodes.add(reference);
    }

    return referencingNodes;
  }

  public Collection<IASTName> findDeclarations(IASTName name, CRefactoringContext context) {
    DeclarationFinder finder =
        new DeclarationFinder(originProject, getIndex(context), getTuLoader(context));
    return finder.findDeclarations(name);
  }

  public Collection<IASTName> findDeclarations(IBinding binding, IIndex index) {
    DeclarationFinder finder = new DeclarationFinder(originProject, index, getTuLoader(index));
    return finder.findDeclarations(binding);
  }

  private static <T extends IASTNode, U extends IASTNode> Maybe<T> getNodeAncestor(U node,
      Class<T> klass) {
    T ancestor = AstUtil.getAncestorOfType(node, klass);
    return maybe(ancestor);
  }

  private static IIndex getIndex(CRefactoringContext context) {
    try {
      return context.getIndex();
    } catch (OperationCanceledException e) {
      throw new MockatorException(e);
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private TranslationUnitLoader getTuLoader(IIndex index) {
    return new TranslationUnitLoader(originProject, index, pm);
  }

  private TranslationUnitLoader getTuLoader(CRefactoringContext context) {
    return new TranslationUnitLoader(originProject, context, pm);
  }
}
