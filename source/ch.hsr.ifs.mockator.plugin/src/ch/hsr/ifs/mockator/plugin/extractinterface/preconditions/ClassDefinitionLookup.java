package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.functional.OptionalUtil;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;


public class ClassDefinitionLookup implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final IASTName classNameToLookup = getNameOfSelectedSpecifier(context.getSelectedName());
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

   private static ICPPASTCompositeTypeSpecifier lookupInAst(final IASTName classNameToLookup) {
      final IBinding binding = classNameToLookup.resolveBinding();

      for (final IASTName name : classNameToLookup.getTranslationUnit().getDefinitionsInAST(binding)) {

         final Optional<ICPPASTCompositeTypeSpecifier> clazz = getClass(name);
         if (clazz.isPresent()) {
            return clazz.get();
         }
      }

      return null;
   }

   private static ICPPASTCompositeTypeSpecifier lookupInIndex(final ExtractInterfaceContext context, final IASTName classNameToLookup) {
      return OptionalUtil.returnIfPresentElseNull(findClassDefinition(classNameToLookup, context), (def) -> def);
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> findClassDefinition(final IASTName classNameToLookup,
            final ExtractInterfaceContext context) {
      final NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
      return lookup.findClassDefinition(classNameToLookup, context.getCRefContext());
   }

   private static IASTName getNameOfSelectedSpecifier(final IASTName selectedName) {
      return OptionalUtil.returnIfPresentElse(getNamedSpecifier(selectedName), (spec) -> spec.getName(), () -> selectedName);
   }

   private static Optional<ICPPASTNamedTypeSpecifier> getNamedSpecifier(final IASTNode originalNode) {
      //TODO use OptHelper
      final ICPPASTNamedTypeSpecifier namedSpec = ASTUtil.getAncestorOfType(originalNode, ICPPASTNamedTypeSpecifier.class);

      if (namedSpec != null) {
         return Optional.of(namedSpec);
      }

      final Optional<IASTDeclSpecifier> declSpec = ASTUtil.getDeclarationSpecifier(originalNode);
      if (declSpec.isPresent() && declSpec.get() instanceof ICPPASTNamedTypeSpecifier) {
         return Optional.of((ICPPASTNamedTypeSpecifier) declSpec.get());
      }

      return Optional.empty();
   }

   private static void warnUserAboutMissingClass(final RefactoringStatus status) {
      status.addFatalError("No class found to extract an interface from!");
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> getSutClass(final IASTName selectedName) {
      return getClass(selectedName).filter((clazz) -> !clazz.getName().toString().equals(selectedName.toString()));
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> getClass(final IASTName originalNode) {
      return Optional.ofNullable(ASTUtil.getAncestorOfType(originalNode, ICPPASTCompositeTypeSpecifier.class));
   }

   private static void rememberClassInformation(final ExtractInterfaceContext context, final ICPPASTCompositeTypeSpecifier dependency,
            final Optional<ICPPASTCompositeTypeSpecifier> sutClass) {
      context.setChosenClass(dependency);
      context.setTuOfChosenClass(dependency.getTranslationUnit());

      sutClass.ifPresent((sc) -> {
         context.setSutClass(sc);
      });
   }
}
