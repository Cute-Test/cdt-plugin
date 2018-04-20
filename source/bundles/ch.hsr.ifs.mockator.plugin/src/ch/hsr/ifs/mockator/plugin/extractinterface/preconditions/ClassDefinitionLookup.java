package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.core.functional.OptionalUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;


public class ClassDefinitionLookup implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {

      Optional<IASTName> nameOfSelectedSpecifier = getNameOfSelectedSpecifier(context.getSelectedName());

      OptionalUtil<ICPPASTCompositeTypeSpecifier> classDefinition = OptionalUtil.of(nameOfSelectedSpecifier).flatFlatMap(nameToLookup -> OptionalUtil
            .of(lookupInAst(nameToLookup)).orElse(lookupInIndex(context, nameToLookup)));

      classDefinition.ifNotPresent(() -> warnUserAboutMissingClass(context.getStatus())).ifPresent(clsDef -> nameOfSelectedSpecifier.ifPresent(
            nameToLookup -> rememberClassInformation(context, clsDef, getSutClass(nameToLookup))));
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> lookupInAst(final IASTName classNameToLookup) {
      final IBinding binding = classNameToLookup.resolveBinding();

      return Stream.of(classNameToLookup.getTranslationUnit().getDefinitionsInAST(binding)).map(name -> getClass(name)).filter(c -> c.isPresent())
            .map(o -> o.get()).findFirst();
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> lookupInIndex(final ExtractInterfaceContext context, final IASTName classNameToLookup) {
      final NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
      return lookup.findClassDefinition(classNameToLookup, context.getCRefContext());
   }

   private static Optional<IASTName> getNameOfSelectedSpecifier(final IASTName selectedName) {
      return OptionalUtil.of(getNamedSpecifier(selectedName.getOriginalNode())).map(ICPPASTNamedTypeSpecifier::getName).orElse(selectedName).get();
   }

   private static Optional<ICPPASTNamedTypeSpecifier> getNamedSpecifier(final IASTNode originalNode) {
      return OptionalUtil.of(CPPVisitor.findAncestorWithType(originalNode, ICPPASTNamedTypeSpecifier.class)).orElse(OptionalUtil.of(ASTUtil
            .getDeclarationSpecifier(originalNode)).mapAs(ICPPASTNamedTypeSpecifier.class)).get();
   }

   private static void warnUserAboutMissingClass(final RefactoringStatus status) {
      status.addFatalError("No class found to extract an interface from!");
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> getSutClass(final IASTName selectedName) {
      return getClass(selectedName).filter((clazz) -> !clazz.getName().toString().equals(selectedName.toString()));
   }

   private static Optional<ICPPASTCompositeTypeSpecifier> getClass(final IASTName originalNode) {
      return CPPVisitor.findAncestorWithType(originalNode, ICPPASTCompositeTypeSpecifier.class);
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
