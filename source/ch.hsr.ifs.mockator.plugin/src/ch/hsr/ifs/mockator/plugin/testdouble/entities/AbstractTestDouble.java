package ch.hsr.ifs.mockator.plugin.testdouble.entities;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

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
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


@SuppressWarnings("restriction")
public abstract class AbstractTestDouble implements TestDouble {

   private final ICPPASTCompositeTypeSpecifier klass;

   public AbstractTestDouble(final ICPPASTCompositeTypeSpecifier klass) {
      this.klass = klass;
   }

   @Override
   public PolymorphismKind getPolymorphismKind() {
      return PolymorphismKind.from(klass);
   }

   @Override
   public IASTName getName() {
      return klass.getName();
   }

   @Override
   public ICPPASTCompositeTypeSpecifier getKlass() {
      return klass;
   }

   @Override
   public Collection<ExistingTestDoubleMemFun> getPublicMemFuns() {
      return AstUtil.getFunctionDefinitions(collectPublicMemFuns()).stream().map(ExistingTestDoubleMemFun::new).collect(Collectors.toList());
   }

   private Collection<IASTDeclaration> collectPublicMemFuns() {
      final PublicMemFunFinder finder = new PublicMemFunFinder(klass, PublicMemFunFinder.ALL_TYPES);
      return finder.getPublicMemFuns();
   }

   @Override
   public Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(final CRefactoringContext context, final ICProject cProject,
         final IProgressMonitor pm) {
      final ReferencingTestFunFinder testFunFinder = new ReferencingTestFunFinder(cProject, klass);
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
      final List<MissingMemberFunction> missingMemFuns = list();
      missingMemFuns.addAll(finder.findMissingMemberFunctions(klass));
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
      Assert.instanceOf(binding, ICPPClassType.class, "Test double must be of class type");
      return (ICPPClassType) binding;
   }

   @Override
   public boolean hasPublicCtor() {
      final Collection<IASTDeclaration> onlyCtors = collectPublicMemFuns().stream().filter(AstUtil::isDeclConstructor).collect(Collectors.toList());
      return !onlyCtors.isEmpty();
   }

   @Override
   public IASTNode getParent() {
      IASTNode currentNode = klass;

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
