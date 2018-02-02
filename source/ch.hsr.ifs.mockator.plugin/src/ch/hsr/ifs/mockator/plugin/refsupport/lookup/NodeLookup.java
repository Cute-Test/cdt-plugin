package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.head;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.OptionalUtil;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;

import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;


public class NodeLookup {

   private final ICProject        originProject;
   private final IProgressMonitor pm;

   public NodeLookup(final ICProject originProject, final IProgressMonitor pm) {
      this.originProject = originProject;
      this.pm = pm;
   }

   public Optional<ICPPASTFunctionDefinition> findFunctionDefinition(final IASTName funName, final CRefactoringContext context) {
      final DefinitionFinder finder = new DefinitionFinder(originProject, getIndex(context), getTuLoader(context));

      final Optional<IASTName> definition = finder.findDefinition(funName);
      return OptionalUtil.returnIfPresentElseEmpty(definition, () -> getNodeAncestor(definition.get(), ICPPASTFunctionDefinition.class));
   }

   public Optional<ICPPASTFunctionDeclarator> findFunctionDeclaration(final IBinding binding, final IIndex index) {
      final DeclarationFinder finder = new DeclarationFinder(originProject, index, getTuLoader(index));
      return getFirstFunDeclaration(binding, finder);
   }

   public Optional<ICPPASTFunctionDeclarator> findFunctionDeclaration(final IASTName funName, final CRefactoringContext context) {
      final DeclarationFinder finder = new DeclarationFinder(originProject, getIndex(context), getTuLoader(context));
      return getFirstFunDeclaration(funName.resolveBinding(), finder);
   }

   private static Optional<ICPPASTFunctionDeclarator> getFirstFunDeclaration(final IBinding funName, final DeclarationFinder finder) {
      // just consider the first declaration found
      final Optional<IASTName> head = head(finder.findDeclarations(funName));
      return OptionalUtil.returnIfPresentElseEmpty(head, () -> getNodeAncestor(head.get(), ICPPASTFunctionDeclarator.class));
   }

   public Optional<ICPPASTCompositeTypeSpecifier> findClassDefinition(final IASTName className, final CRefactoringContext context) {
      final DefinitionFinder finder = new DefinitionFinder(originProject, getIndex(context), getTuLoader(context));
      return OptionalUtil.returnIfPresentElseEmpty(finder.findDefinition(className), (def) -> getClassAncestor(def));
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> getClassAncestor(final IASTNode node) {
      return getNodeAncestor(node, ICPPASTCompositeTypeSpecifier.class);
   }

   public <T> Optional<ICPPASTCompositeTypeSpecifier> findClassDefinition(final IBinding binding, final IIndex index) {
      final DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));
      return OptionalUtil.returnIfPresentElseEmpty(finder.findDefinition(binding), (def) -> getClassAncestor(def));
   }

   public Optional<ICPPASTCompositeTypeSpecifier> findClassDefinition(final String name, final IIndex index) {
      final DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));
      return OptionalUtil.returnIfPresentElseEmpty(finder.findDefinition(name), (def) -> getClassAncestor(def));
   }

   public Collection<ICPPASTFunctionDefinition> findReferencingFunctions(final IASTName name, final CRefactoringContext context) {
      final TranslationUnitLoader tuLoader = getTuLoader(context);
      final ReferenceFinder finder = new ReferenceFinder(originProject, getIndex(context), tuLoader);
      final List<ICPPASTFunctionDefinition> referencingFunctions = new ArrayList<>();

      for (final IASTNode optReference : finder.findReferences(name)) {
         getNodeAncestor(optReference, ICPPASTFunctionDefinition.class).ifPresent((fun) -> referencingFunctions.add(fun));
      }

      return referencingFunctions;
   }

   public Optional<ICPPASTTemplateDeclaration> findTemplateDefinition(final IBinding binding, final IIndex index) {
      final DefinitionFinder finder = new DefinitionFinder(originProject, index, getTuLoader(index));

      return OptionalUtil.returnIfPresentElseEmpty(finder.findDefinition(binding), (def) -> getNodeAncestor(def, ICPPASTTemplateDeclaration.class));
   }

   public Collection<IASTName> findReferencingNames(final IASTName name, final CRefactoringContext context) {
      final TranslationUnitLoader tuLoader = getTuLoader(context);
      final ReferenceFinder finder = new ReferenceFinder(originProject, getIndex(context), tuLoader);
      final List<IASTName> referencingNodes = new ArrayList<>();

      for (final IASTName reference : finder.findReferences(name)) {
         referencingNodes.add(reference);
      }

      return referencingNodes;
   }

   public Collection<IASTName> findDeclarations(final IASTName name, final CRefactoringContext context) {
      final DeclarationFinder finder = new DeclarationFinder(originProject, getIndex(context), getTuLoader(context));
      return finder.findDeclarations(name);
   }

   public Collection<IASTName> findDeclarations(final IBinding binding, final IIndex index) {
      final DeclarationFinder finder = new DeclarationFinder(originProject, index, getTuLoader(index));
      return finder.findDeclarations(binding);
   }

   private static <T extends IASTNode, U extends IASTNode> Optional<T> getNodeAncestor(final U node, final Class<T> clazz) {
      return Optional.ofNullable(ASTUtil.getAncestorOfType(node, clazz));
   }

   private static IIndex getIndex(final CRefactoringContext context) {
      try {
         return context.getIndex();
      } catch (final OperationCanceledException e) {
         throw new ILTISException(e).rethrowUnchecked();
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private TranslationUnitLoader getTuLoader(final IIndex index) {
      return new TranslationUnitLoader(originProject, index, pm);
   }

   private TranslationUnitLoader getTuLoader(final CRefactoringContext context) {
      return new TranslationUnitLoader(originProject, context, pm);
   }
}
