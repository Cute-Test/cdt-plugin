package ch.hsr.ifs.cute.mockator.testdouble.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;

import ch.hsr.ifs.cute.mockator.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.cute.mockator.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.cute.mockator.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.cute.mockator.testdouble.PolymorphismKind;


public abstract class AbstractTestDouble implements TestDouble {

   private final ICPPASTCompositeTypeSpecifier clazz;

   public AbstractTestDouble(final ICPPASTCompositeTypeSpecifier clazz) {
      this.clazz = clazz;
   }

   @Override
   public PolymorphismKind getPolymorphismKind() {
      return PolymorphismKind.from(clazz);
   }

   @Override
   public IASTName getName() {
      return clazz.getName();
   }

   @Override
   public ICPPASTCompositeTypeSpecifier getKlass() {
      return clazz;
   }

   @Override
   public Collection<ExistingTestDoubleMemFun> getPublicMemFuns() {
      return ASTUtil.getFunctionDefinitions(collectPublicMemFuns()).stream().map(ExistingTestDoubleMemFun::new).collect(Collectors.toList());
   }

   private Collection<IASTDeclaration> collectPublicMemFuns() {
      final PublicMemFunFinder finder = new PublicMemFunFinder(clazz, PublicMemFunFinder.ALL_TYPES);
      return finder.getPublicMemFuns();
   }

   @Override
   public Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(final CRefactoringContext context, final ICProject cProject,
         final IProgressMonitor pm) {
      final ReferencingTestFunFinder testFunFinder = new ReferencingTestFunFinder(cProject, clazz);
      return testFunFinder.findByIndexLookup(context, pm);
   }

   @Override
   public boolean hasOnlyStaticFunctions(final Collection<? extends MissingMemberFunction> missingMemFuns) {
      return hasOnlyStatics(getPublicMemFuns()) && hasOnlyStatics(missingMemFuns);
   }

   private static <T extends TestDoubleMemFun> boolean hasOnlyStatics(final Collection<T> testDoubleFuns) {
      final Collection<T> nonStatics = testDoubleFuns.stream().filter((Predicate<T>) (memFun) -> !memFun.isStatic()).collect(Collectors.toList());
      return nonStatics.isEmpty();
   }

   @Override
   public Collection<? extends MissingMemberFunction> collectMissingMemFuns(final MissingMemFunFinder finder, final CppStandard cppStd) {
      final List<MissingMemberFunction> missingMemFuns = new ArrayList<>();
      missingMemFuns.addAll(finder.findMissingMemberFunctions(clazz));
      addDefaultCtorIfNecessary(cppStd, missingMemFuns);
      return missingMemFuns;
   }

   private void addDefaultCtorIfNecessary(final CppStandard cppStd, final List<MissingMemberFunction> missing) {
      final DefaultCtorProvider provider = getDefaultCtorProvider(cppStd);
      provider.createMissingDefaultCtor(missing).ifPresent((defaultCtor) -> missing.add(0, defaultCtor));
   }

   @Override
   public abstract DefaultCtorProvider getDefaultCtorProvider(CppStandard cppStd);

   @Override
   public void addMissingMemFuns(final Collection<? extends MissingMemberFunction> missingMemFuns, final ClassPublicVisibilityInserter inserter,
         final CppStandard cppStd) {
      for (final MissingMemberFunction m : missingMemFuns) {
         final TestDoubleMemFunImplStrategy implStrategy = getImplStrategy(cppStd);
         inserter.insert(m.createFunctionDefinition(implStrategy, cppStd));
      }
   }

   protected abstract TestDoubleMemFunImplStrategy getImplStrategy(CppStandard cppStd);

   @Override
   public abstract void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor, CppStandard cppStd);

   @Override
   public ICPPClassType getClassType() {
      final IBinding binding = getName().resolveBinding();
      ILTISException.Unless.assignableFrom("Test double must be of class type", ICPPClassType.class, binding);
      return (ICPPClassType) binding;
   }

   @Override
   public boolean hasPublicCtor() {
      final Collection<IASTDeclaration> onlyCtors = collectPublicMemFuns().stream().filter(ASTUtil::isDeclConstructor).collect(Collectors.toList());
      return !onlyCtors.isEmpty();
   }

   @Override
   public IASTNode getParent() {
      IASTNode currentNode = clazz;

      while (currentNode != null) {
         if (currentNode instanceof ICPPASTFunctionDefinition) { return ((ICPPASTFunctionDefinition) currentNode).getBody(); }

         if (currentNode instanceof ICPPASTNamespaceDefinition) { return currentNode; }

         currentNode = currentNode.getParent();
      }
      return null;
   }

   @Override
   public String toString() {
      return getName().toString();
   }
}
